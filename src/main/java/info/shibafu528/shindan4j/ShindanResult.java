package info.shibafu528.shindan4j;

import java.io.Serializable;

/**
 * Created by shibafu on 14/06/15.
 */
public class ShindanResult implements Serializable {
    private Shindan page;
    private String name;
    private String displayResult;
    private String shareResult;

    public ShindanResult(Shindan page, String name, String displayResult, String shareResult) {
        this.page = page;
        this.name = name;
        this.displayResult = displayResult;
        this.shareResult = shareResult;
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

    public String getShareResult() {
        return shareResult;
    }
}
