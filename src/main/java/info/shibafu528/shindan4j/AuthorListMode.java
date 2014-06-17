package info.shibafu528.shindan4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by shibafu on 14/06/17.
 */
public enum AuthorListMode {
    NEW,
    POPULAR,
    NAME;

    private static final String LISTPAGE_URL = "http://shindanmaker.com/author/";

    public String toUrlString(String screenName, int page, String... queries) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(LISTPAGE_URL);
        sb.append(screenName);
        sb.append("?mode=");
        sb.append(ordinal());
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
