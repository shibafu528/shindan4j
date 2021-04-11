package info.shibafu528.shindan4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shibafu on 14/06/15.
 */
public class ShindanMaker {
    private static String userAgent = "shindan4j(https://github.com/shibafu528/shindan4j)";
    private static int timeout = 20000;

    private static final Pattern[] PROFILE_PATTERNS = new Pattern[]{
            Pattern.compile("作成した診断：([0-9,]+) 個"),
            Pattern.compile("診断された回数：([0-9,]+) 回")
    };

    private ShindanMaker() {}

    public static int getTimeout() {
        return timeout;
    }

    public static void setTimeout(int millis) {
        ShindanMaker.timeout = millis;
    }

    public static String getUserAgent() {
        return userAgent;
    }

    public static void setUserAgent(String userAgent) {
        ShindanMaker.userAgent = userAgent;
    }

    public static Shindan getShindan(int pageId) throws IOException {
        String url = "https://shindanmaker.com/" + pageId;
        Document doc = getDocument(url);
        //タイトル、説明文を取得
        Element titleElement = doc.getElementById("shindanTitle");
        if (titleElement == null) {
            throw new IOException("タイトル (#shindanTitle) がHTML上に見つかりません\nURL:" + url);
        }
        String title = titleElement.text().trim();
        Element descElement = doc.getElementById("shindanDescription");
        if (descElement == null) {
            throw new IOException("説明文 #shindanDescription がHTML上に見つかりません\nURL:" + url);
        }
        String desc = descElement.text().trim();
        //テーマラベルを取得
        List<String> theme = new ArrayList<>();
        Elements themes = doc.select("a.label-theme");
        for (Element e : themes) {
            theme.add(e.text());
        }
        //Fav数を取得
        int favs = 0;
        Elements favlabel = doc.select(".label-favorite");
        if (favlabel.first() != null) {
            Matcher m = Pattern.compile("(\\d+)").matcher(favlabel.first().text());
            if (m.find()) {
                favs = Integer.valueOf(m.group(1));
            }
        }
        //作者名を取得
        Elements elemAuthor = doc.select(".label-user-shindan");
        String author = ((elemAuthor != null)? elemAuthor.first().text().replace("@", "") : null);
        //アクセスカウンターを取得
        int accessCounter = 0;
        NumberFormat numberFormat = NumberFormat.getInstance();
        Element doneNumber = doc.getElementById("donenumber");
        if (doneNumber != null) {
            try {
                accessCounter = numberFormat.parse(doneNumber.text()).intValue();
            } catch (ParseException ignored) {}
        }
        //結果パターン数を取得
        // TODO: 2021/4/11 リニューアル以降か分からないけど「結果パターン ? 通り」と表現されている場合もある。未対応。
        String resultPatterns = "0";
        Element elemResultPattern = null;
        for (Element el : doc.select("#shindanInfo .label-etc")) {
            if (el.text().trim().startsWith("結果パターン")) {
                elemResultPattern = el.select("b").first();
            }
        }
        if (elemResultPattern != null) {
            resultPatterns = elemResultPattern.text().replaceAll("[^0-9]", "");
        }
        //POST先URLを取得
        String postUrl = doc.getElementById("shindanForm").attr("action");
        if (!postUrl.startsWith("https://shindanmaker.com")) {
            // NOTE: 2021/4/11 リニューアル以降たぶん不要
            postUrl = "https://shindanmaker.com" + postUrl;
        }
        //インスタンスを返す
        return new ShindanPage(
                pageId, title, desc, author,
                "", theme,
                accessCounter, favs, resultPatterns,
                doc.select("a.label-hot").first() != null,
                doc.select("a.label-pickup").first() != null,
                postUrl);
    }

    public static List<Shindan> getList(ListMode mode, int page) throws IOException {
        return getListElements(getDocument(mode.toUrlString(page)));
    }

    public static List<Shindan> search(String query, int page, boolean orderByNew) throws IOException {
        if (orderByNew) {
            return getListElements(getDocument(ListMode.SEARCH.toUrlString(page, "q", query, "order", "new")));
        } else {
            return getListElements(getDocument(ListMode.SEARCH.toUrlString(page, "q", query)));
        }
    }

    public static List<Shindan> themeSearch(String theme, int page, boolean orderByNew) throws IOException {
        if (orderByNew) {
            return getListElements(getDocument(ListMode.THEME.toUrlString(page, "tag", theme, "order", "new")));
        }else {
            return getListElements(getDocument(ListMode.THEME.toUrlString(page, "tag", theme)));
        }
    }

    public static ShindanAuthor getAuthor(AuthorListMode mode, String screenName, int page) throws IOException {
        if (screenName.startsWith("@")) {
            screenName = screenName.substring(1);
        }
        Document doc = getDocument(mode.toUrlString(screenName, page));
        List<Shindan> summaries = getListElements(doc);
        String name = screenName;
        int[] counters = new int[PROFILE_PATTERNS.length];
        Elements profile = doc.select("div[class=authorprofile_s]");
        for (Element span : profile.select("span")) {
            for (int i = 0; i < PROFILE_PATTERNS.length; i++) {
                Matcher m = PROFILE_PATTERNS[i].matcher(span.text());
                if (m.find()) {
                    counters[i] = Integer.valueOf(m.group(1).replace(",", ""));
                }
            }
        }
        return new ShindanAuthor(summaries, name, screenName, counters[0], counters[1]);
    }

    private static Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).userAgent(userAgent).timeout(timeout).get();
    }

    private static List<Shindan> getListElements(Document doc) throws IOException {
        List<Shindan> summaries = new ArrayList<>();
        //リストの各要素の親をとる
        Elements tables = doc.select("table[class=list_list]");
        //各要素をパース
        //  2014/3/4以降ページ構造変化により、trタグ2つで1組となった、めんどくさい
        //TODO: もっととれる要素あると思う
        Summary summary = null;
        for (Element e : tables.select("tr")) {
            if (summary == null) {
                //タイトルとID
                Elements elemTitle = e.select("a[class=list_title]");
                String title = elemTitle.text();
                if (title == null || "".equals(title)) {
                    //タイトル無いやつとか少なくとも診断では無さそうなので飛ばす
                    continue;
                }
                int pageId = Integer.valueOf(elemTitle.attr("href").replaceAll("[a-zA-Z-/]", ""));
                //カウンター
                Elements elemNum = e.select("span[class=list_num]");
                String regex = "\\d+";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(elemNum.text().replaceAll(",", ""));
                int counter = 0;
                if (matcher.find()) {
                    counter = Integer.valueOf(matcher.group());
                }
                //組立機に突っ込む
                summary = new Summary()
                        .setPageId(pageId)
                        .setTitle(title)
                        .setAccessCount(counter);
            } else {
                //作者
                Elements elemAuthor = e.select("span[class=list_author]");
                String author = ((elemAuthor != null)? elemAuthor.select("a").text() : null);
                //テーマラベル
                Elements elemTheme = e.select("a[class=themelabel]");
                List<String> themelabel = new ArrayList<>();
                if (elemTheme != null) {
                    for (Element et : elemTheme) {
                        themelabel.add(et.text());
                    }
                }
                //ハッシュタグ
                Elements elemHashtag = e.select("span[class=hushtag]");
                String hashtag = ((elemHashtag != null)? elemHashtag.text() : null);
                //概要
                Elements elemDesc = e.select(".list_description_text");
                String desc = ((elemDesc != null) ? elemDesc.text() : "");
                //インスタンス作って要素リストに格納
                summary.setAuthorName(author)
                        .setHashTag(hashtag)
                        .setThemes(themelabel)
                        .setDescription(desc);
                assert summary.isCompleteElements();
                summaries.add(summary);
                summary = null;
            }
        }
        return summaries;
    }
}
