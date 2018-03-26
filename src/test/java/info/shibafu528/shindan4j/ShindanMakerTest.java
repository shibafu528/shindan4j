package info.shibafu528.shindan4j;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShindanMakerTest {

    @Test
    public void testGetShindan() throws Exception {
        int[] shindans = {438894, 459592, 324271, 181846, 220337, 18312, 201828, 322469};
        for (int id : shindans) {
            Shindan s = ShindanMaker.getShindan(id);
            Assert.assertNotNull(s);
            System.out.println(s);
        }
    }

    @Test
    public void testPostShindan() throws Exception {
        ShindanResult result = ShindanMaker.getShindan(258437).shindan("名前name");
        System.out.println(result.getDisplayResult() + " : " + result.getPage());
        Assert.assertEquals("名前name.", result.getDisplayResult());
    }

    @Test
    public void testGetList() throws Exception {
        List<ListMode> using = new ArrayList<>(Arrays.asList(ListMode.values()));
        using.remove(ListMode.SEARCH);
        using.remove(ListMode.THEME);
        for (ListMode mode : using) {
            System.out.println("====[" + mode.name() + "]====");
            List<Shindan> sl = ShindanMaker.getList(mode, 1);
            Assert.assertNotNull(sl);
            for (Shindan shindan : sl) {
                System.out.println(shindan);
            }
        }
    }

    @Test
    public void testSearch() throws Exception {
        List<Shindan> sl = ShindanMaker.search("おしりターボ", 1, false);
        Assert.assertNotNull(sl);
        for (Shindan shindan : sl) {
            System.out.println(shindan);
            if (shindan.getPageId() == 438894) {
                Assert.assertEquals("おしりターボ", shindan.getTitle());
                Assert.assertEquals("ボーボボ", shindan.getDescription());
                Assert.assertEquals("@Getaji", shindan.getAuthorName());
                Assert.assertThat(shindan.getThemes(), CoreMatchers.hasItem("身内ネタ"));
            }
        }
    }

    @Test
    public void testThemeSearch() throws Exception {
        List<Shindan> sl = ShindanMaker.themeSearch("人生", 1, false);
        Assert.assertNotNull(sl);
        for (Shindan shindan : sl) {
            System.out.println(shindan);
        }
    }

    @Test
    public void testGetAuthor() throws Exception {
        ShindanAuthor author = ShindanMaker.getAuthor(AuthorListMode.NEW, "Getaji", 1);
        Assert.assertNotNull(author);
        System.out.println(author);
        for (Shindan shindan : author) {
            System.out.println(shindan);
        }
    }
}