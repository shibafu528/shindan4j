package info.shibafu528.shindan4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by shibafu on 14/06/15.
 */
public interface Shindan extends Serializable{
    int getPageId();

    String getPageUrl();

    String getTitle();

    String getDescription();

    String getAuthorName();

    String getHashTag();

    List<String> getThemes();

    int getAccessCount();

    int getFavoritedCount();

    int getResultPatterns();

    boolean isHot();

    boolean isPickup();

    ShindanResult shindan(String name) throws IOException;
}
