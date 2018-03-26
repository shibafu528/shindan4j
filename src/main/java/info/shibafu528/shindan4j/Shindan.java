package info.shibafu528.shindan4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by shibafu on 14/06/15.
 */
public interface Shindan extends Serializable{
    public int getPageId();

    public String getPageUrl();

    public String getTitle();

    public String getDescription();

    public String getAuthorName();

    public String getHashTag();

    public List<String> getThemes();

    public int getAccessCount();

    public int getFavoritedCount();

    int getResultPatterns();

    public boolean isHot();

    public boolean isPickup();

    public ShindanResult shindan(String name) throws IOException;
}
