package info.shibafu528.shindan4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by shibafu on 14/06/15.
 */
public enum ListMode {
    NEW("0"),
    BEST("1"),
    HOT,
    PICKUP,
    DAILY,
    MONTHLY,
    FAVORITE("fav"),
    FAVORITE_HOT("favhot"),
    PICTURE("pic"),
    MOVIE("mov"),
    SEARCH,
    THEME("tag");

    private static final String LISTPAGE_URL = "https://shindanmaker.com/c/list?";

    private String value;

    private ListMode() {
        this.value = name().toLowerCase();
    }

    private ListMode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public String toUrlString(int page, String... queries) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(LISTPAGE_URL);
        sb.append("mode=");
        sb.append(toString());
        sb.append("&p=");
        sb.append(page);
        String key = null;
        for (String query : queries) {
            if (key == null) {
                key = query;
            } else {
                sb.append("&");
                sb.append(key);
                sb.append("=");
                sb.append(URLEncoder.encode(query, "utf-8"));
                key = null;
            }
        }
        return sb.toString();
    }
}
