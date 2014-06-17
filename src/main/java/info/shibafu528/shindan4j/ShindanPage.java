package info.shibafu528.shindan4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;

/**
 * Created by shibafu on 14/06/15.
 */
class ShindanPage extends Summary{

    private String postTo;

    ShindanPage(int pageId, String title, String description, String authorName,
                String hashTag, List<String> themes,
                int accessCount, int favoritedCount, boolean isHot, boolean isPickup,
                String postTo) {
        super(pageId, title, description, authorName, hashTag, themes, accessCount, favoritedCount, isHot, isPickup);
        this.postTo = postTo;
    }

    @Override
    public ShindanResult shindan(String name) throws IOException {
        Document doc = Jsoup.connect(postTo).data("u", name).timeout(20000).post();
        //結果を取得
        Element shareElem = doc.select("textarea[onclick=this.focus();this.select()]").first();
        if (shareElem == null) {
            throw new IOException("textarea[onclick=this.focus();this.select()]がHTML上に見つかりません\nURL:" + getPageUrl());
        }
        String share = shareElem.text();
        String display = share.replaceAll("[ \n]" + getPageUrl() + "$", "");
        return new ShindanResult(this, name, display, share);
    }

    @Override
    public String toString() {
        return String.format("「%s」by %s (%s) / %s (f%d,%s,%s)", getTitle(), getAuthorName(), getPageUrl(),
                getDescription(), getFavoritedCount(),
                isHot()? "HOT":"",
                isPickup()? "PICKUP":"");
    }
}
