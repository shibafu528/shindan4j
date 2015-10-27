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

    ShindanPage(int pageId, String title, String description, String authorName,
                String hashTag, List<String> themes,
                int accessCount, int favoritedCount, boolean isHot, boolean isPickup) {
        super(pageId, title, description, authorName, hashTag, themes, accessCount, favoritedCount, isHot, isPickup);
    }

    @Override
    public ShindanResult shindan(String name) throws IOException {
        Document doc = Jsoup.connect(getPageUrl()).data("u", name).timeout(20000).post();
        //結果を取得
        Element shareElem = doc.select("textarea#copy_text_140").first();
        if (shareElem == null) {
            throw new IOException("textarea#copy_text_140 がHTML上に見つかりません\nURL:" + getPageUrl());
        }
        String share = shareElem.text();
        String display = share.replaceAll("[ \n]" + getPageUrl() + "$", "");
        return new ShindanResult(this, name, display, share);
    }

    @Override
    public String toString() {
        return String.format("「%s」by %s (%s) / %s (a%d,f%d,%s,%s)", getTitle(), getAuthorName(), getPageUrl(),
                getDescription(),
                getAccessCount(), getFavoritedCount(),
                isHot()? "HOT":"",
                isPickup()? "PICKUP":"");
    }
}
