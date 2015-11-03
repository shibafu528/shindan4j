package info.shibafu528.shindan4j;

import java.io.IOException;
import java.util.List;

/**
 * Created by shibafu on 14/06/15.
 */
class Summary implements Shindan {

    private int pageId;
    private String pageUrl;
    private String title;
    private String description;
    private String authorName;
    private String hashTag;
    private List<String> themes;
    private int accessCount;
    private int favoritedCount;
    private boolean isHot;
    private boolean isPickup;

    Summary() {}

    Summary(int pageId, String title, String description, String authorName, String hashTag, List<String> themes, int accessCount, int favoritedCount, boolean isHot, boolean isPickup) {
        setPageId(pageId);
        this.title = title;
        this.description = description;
        this.authorName = authorName;
        this.hashTag = hashTag;
        this.themes = themes;
        this.accessCount = accessCount;
        this.favoritedCount = favoritedCount;
        this.isHot = isHot;
        this.isPickup = isPickup;
    }

    public Summary setPageId(int pageId) {
        this.pageId = pageId;
        this.pageUrl = "https://shindanmaker.com/" + pageId;
        return this;
    }

    public Summary setTitle(String title) {
        this.title = title;
        return this;
    }

    public Summary setDescription(String description) {
        this.description = description;
        return this;
    }

    public Summary setAuthorName(String authorName) {
        this.authorName = authorName;
        return this;
    }

    public Summary setHashTag(String hashTag) {
        this.hashTag = hashTag;
        return this;
    }

    public Summary setThemes(List<String> themes) {
        this.themes = themes;
        return this;
    }

    public Summary setAccessCount(int accessCount) {
        this.accessCount = accessCount;
        return this;
    }

    public Summary setFavoritedCount(int favoritedCount) {
        this.favoritedCount = favoritedCount;
        return this;
    }

    public Summary setHot(boolean isHot) {
        this.isHot = isHot;
        return this;
    }

    public Summary setPickup(boolean isPickup) {
        this.isPickup = isPickup;
        return this;
    }

    public int getPageId() {
        return pageId;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getHashTag() {
        return hashTag;
    }

    public List<String> getThemes() {
        return themes;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public int getFavoritedCount() {
        return favoritedCount;
    }

    public boolean isHot() {
        return isHot;
    }

    public boolean isPickup() {
        return isPickup;
    }

    public boolean isCompleteElements() {
        return  pageId > 0 &&
                pageUrl != null &&
                title != null &&
                description != null &&
                authorName != null &&
                hashTag != null &&
                themes != null;
    }

    @Override
    public ShindanResult shindan(String name) throws IOException{
        return ShindanMaker.getShindan(getPageId()).shindan(name);
    }

    @Override
    public String toString() {
        return String.format("「%s」by %s (%s) / %s (f%d,%s,%s)", getTitle(), getAuthorName(), getPageUrl(),
                getDescription(), getFavoritedCount(),
                isHot()? "HOT":"",
                isPickup()? "PICKUP":"");
    }
}
