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

    private static final Pattern[] INLINELIST_PATTERNS = new Pattern[] {
            Pattern.compile("(\\d{1,3}(?:,\\d{3})*)人が診断"),
            Pattern.compile("結果パターン(\\d{1,3}(?:,\\d{3})*)通り")
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
        //METAタグを取得
        Elements meta = doc.select("meta");
        //タイトル、説明文を取得
        Element titleElement = meta.select("*[property=og:title]").first();
        if (titleElement == null) {
            throw new IOException("タイトル<meta property=\"og:title\">がHTML上に見つかりません\nURL:" + url);
        }
        String title = titleElement.attr("content");
        Element descElement = meta.select("*[property=og:description]").first();
        if (descElement == null) {
            throw new IOException("説明文<meta property=\"og:description\">がHTML上に見つかりません\nURL:" + url);
        }
        String desc = descElement.attr("content");
        //テーマラベルを取得
        List<String> theme = new ArrayList<>();
        Elements themes = doc.select("a[class=themelabel]");
        for (Element e : themes) {
            theme.add(e.text());
        }
        //Fav数を取得
        int favs = 0;
        Elements favlabel = doc.select("a[class=favlabel]");
        if (favlabel.first() != null) {
            Matcher m = Pattern.compile("★(\\d+)").matcher(favlabel.first().text());
            if (m.find()) {
                favs = Integer.valueOf(m.group(1));
            }
        }
        //作者名を取得
        Elements elemAuthor = doc.select("a[href^=/author/]:not(:has(button))");
        String author = ((elemAuthor != null)? elemAuthor.select("a").text() : null);
        //アクセスカウンターや結果パターン数を取得する
        //TODO: 結果パターン数も活用したい
        NumberFormat numberFormat = NumberFormat.getInstance();
        Element inlinelist = doc.select("ul[class=inlinelist]").first();
        int inlinelistNums[] = new int[INLINELIST_PATTERNS.length];
        for (Element element : inlinelist.children()) {
            for (int i = 0; i < INLINELIST_PATTERNS.length; i++) {
                Matcher matcher = INLINELIST_PATTERNS[i].matcher(element.text());
                if (matcher.find()) {
                    try {
                        inlinelistNums[i] = numberFormat.parse(matcher.group(1)).intValue();
                    } catch (ParseException ignored) {}
                }
            }
        }
        //説明文の接尾辞を削除する
        desc = desc.substring(0, desc.length() - 9);
        //POST先URLを取得
        String postUrl = doc.select("form#form").first().attr("action");
        postUrl = "https://shindanmaker.com" + postUrl;
        //インスタンスを返す
        return new ShindanPage(
                pageId, title, desc, author,
                "", theme,
                inlinelistNums[0], favs,
                doc.select("a[class=hotlabel]").first() != null,
                doc.select("a[class=pickuplabel]").first() != null,
                postUrl
        );
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
                Elements elemDesc = e.select("td[class=list_description]");
                Pattern descPattern = Pattern.compile("^(.+)\\n<div");
                Matcher descMatcher = descPattern.matcher(elemDesc.html());
                String desc;
                if (descMatcher.find()) {
                    desc = Jsoup.parseBodyFragment(descMatcher.group(1)).text();
                } else {
                    desc = "";
                }
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
