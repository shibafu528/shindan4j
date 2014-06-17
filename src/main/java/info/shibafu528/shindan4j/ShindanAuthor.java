package info.shibafu528.shindan4j;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by shibafu on 14/06/17.
 */
public class ShindanAuthor extends ArrayList<Shindan> {
    private String name;
    private String screenName;
    private int createdShindanCount;
    private int totalAccessCount;

    public ShindanAuthor(Collection<? extends Shindan> c,
                         String name, String screenName, int createdShindanCount, int totalAccessCount) {
        super(c);
        this.name = name;
        this.screenName = screenName;
        this.createdShindanCount = createdShindanCount;
        this.totalAccessCount = totalAccessCount;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public int getCreatedShindanCount() {
        return createdShindanCount;
    }

    public int getTotalAccessCount() {
        return totalAccessCount;
    }

    @Override
    public String toString() {
        return String.format("%s @%s created:%d, access:%d",
                getName(),
                getScreenName(),
                getCreatedShindanCount(),
                getTotalAccessCount());
    }
}
