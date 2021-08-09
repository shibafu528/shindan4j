package info.shibafu528.shindan4j;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by shibafu on 14/06/15.
 */
class ShindanPage extends Summary{

    private final String postUrl;

    ShindanPage(int pageId, String title, String description, String authorName,
                String hashTag, List<String> themes,
                int accessCount, int favoritedCount, String resultPatterns, boolean isHot, boolean isPickup, String postUrl) {
        super(pageId, title, description, authorName, hashTag, themes, accessCount, favoritedCount, resultPatterns, isHot, isPickup);
        this.postUrl = postUrl;
    }

    @Override
    public ShindanResult shindan(String name) throws IOException {
        Session session = beginSession();
        Document doc = Jsoup.connect(postUrl)
                .userAgent(ShindanMaker.getUserAgent())
                .timeout(ShindanMaker.getTimeout())
                .cookies(session.cookies)
                .data("_token", session.token)
                .data("shindanName", name)
                .post();
        //結果を取得
        Element fullElem = doc.select("textarea#copy-textarea-all").first();
        Element shareElem = doc.select("textarea#copy-textarea-140").first();
        if (shareElem == null) {
            throw new IOException("textarea#copy-textarea-140 がHTML上に見つかりません\nURL:" + getPageUrl());
        }
        //フルの診断結果が格納されている要素を判定
        Element longestResultElem;
        if (fullElem == null || fullElem.text().trim().equals("")) {
            //全文コピペ用textareaがないか空の場合、通常のコピペ用textareaを使う
            longestResultElem = shareElem;
        } else {
            longestResultElem = fullElem;
        }
        String displayResult = longestResultElem.text().replaceAll("[ \n]" + getPageUrl() + "$", "");
        return new ShindanResult(this, name, displayResult, longestResultElem.text(), shareElem.text());
    }

    @Override
    public String toString() {
        return String.format("「%s」by %s (%s) / %s (a%d,f%d,p%s,%s,%s)", getTitle(), getAuthorName(), getPageUrl(),
                getDescription(),
                getAccessCount(), getFavoritedCount(), getResultPatterns(),
                isHot()? "HOT":"",
                isPickup()? "PICKUP":"");
    }

    private Session beginSession() throws IOException {
        Connection.Response res = Jsoup.connect(postUrl)
                .userAgent(ShindanMaker.getUserAgent())
                .timeout(ShindanMaker.getTimeout())
                .method(Connection.Method.GET)
                .execute();
        Document doc = res.parse();

        String token = "";
        Element tokenElement = doc.select("#shindanForm input[name=_token]").first();
        if (tokenElement != null) {
            token = tokenElement.attr("value").trim();
        }
        return new Session(res.cookies(), token);
    }

    private static class Session {
        public final Map<String, String> cookies;
        public final String token;

        public Session(Map<String, String> cookies, String token) {
            this.cookies = cookies;
            this.token = token;
        }
    }
}
