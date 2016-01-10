package info.shibafu528.shindan4j;

import java.io.Serializable;

/**
 * Created by shibafu on 14/06/15.
 */
public class ShindanResult implements Serializable {
    private Shindan page;
    private String name;
    private String displayResult;
    private String fullShareResult;
    private String shortShareResult;

    public ShindanResult(Shindan page, String name, String displayResult, String fullShareResult, String shortShareResult) {
        this.page = page;
        this.name = name;
        this.displayResult = displayResult;
        this.fullShareResult = fullShareResult;
        this.shortShareResult = shortShareResult;
    }

    public Shindan getPage() {
        return page;
    }

    public String getName() {
        return name;
    }

    public String getDisplayResult() {
        return displayResult;
    }

    @Deprecated
    public String getShareResult() {
        return shortShareResult;
    }

    public String getFullShareResult() {
        return fullShareResult;
    }

    public String getShortShareResult() {
        return shortShareResult;
    }
}
