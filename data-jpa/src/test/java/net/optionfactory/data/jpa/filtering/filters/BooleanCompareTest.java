package net.optionfactory.data.jpa.filtering.filters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.optionfactory.data.jpa.HibernateTestConfig;
import net.optionfactory.data.jpa.filtering.FilterRequest;
import net.optionfactory.data.jpa.filtering.Flag;
import net.optionfactory.data.jpa.filtering.FlagsRepository;
import net.optionfactory.data.jpa.filtering.filters.spi.Filters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = HibernateTestConfig.class)
@Transactional
public class BooleanCompareTest {

    @Autowired
    private FlagsRepository flags;

    @Before
    public void setup() {
        final Flag trueFlag = new Flag();
        trueFlag.id = 1;
        trueFlag.value = true;
        final Flag falseFlag = new Flag();
        falseFlag.id = 2;
        falseFlag.value = false;
        flags.saveAll(Arrays.asList(trueFlag, falseFlag));
    }

    @Test
    public void canFilterBooleanValueWithDefaultOptions() {
        Assert.assertEquals(ids(1L), idsIn(flags.findAll(filter("javaBoolean", "true"), Pageable.unpaged())));
        Assert.assertEquals(ids(1L), idsIn(flags.findAll(filter("javaBoolean", "True"), Pageable.unpaged())));
        Assert.assertEquals(ids(1L), idsIn(flags.findAll(filter("javaBoolean", "TRUE"), Pageable.unpaged())));
        Assert.assertEquals(ids(2L), idsIn(flags.findAll(filter("javaBoolean", "false"), Pageable.unpaged())));
        Assert.assertEquals(ids(2L), idsIn(flags.findAll(filter("javaBoolean", "False"), Pageable.unpaged())));
        Assert.assertEquals(ids(2L), idsIn(flags.findAll(filter("javaBoolean", "FALSE"), Pageable.unpaged())));
    }

    @Test
    public void canFilterBooleanValueWithCustomValuesIgnoringCase() {
        Assert.assertEquals(ids(1L), idsIn(flags.findAll(filter("yesNoBoolean", "yes"), Pageable.unpaged())));
        Assert.assertEquals(ids(1L), idsIn(flags.findAll(filter("yesNoBoolean", "Yes"), Pageable.unpaged())));
        Assert.assertEquals(ids(1L), idsIn(flags.findAll(filter("yesNoBoolean", "YES"), Pageable.unpaged())));
        Assert.assertEquals(ids(2L), idsIn(flags.findAll(filter("yesNoBoolean", "no"), Pageable.unpaged())));
        Assert.assertEquals(ids(2L), idsIn(flags.findAll(filter("yesNoBoolean", "No"), Pageable.unpaged())));
        Assert.assertEquals(ids(2L), idsIn(flags.findAll(filter("yesNoBoolean", "NO"), Pageable.unpaged())));
    }

    @Test
    public void canFilterBooleanValueWithCustomValuesMatchingCase() {
        Assert.assertEquals(ids(1L), idsIn(flags.findAll(filter("YNMatchCaseBoolean", "Y"), Pageable.unpaged())));
        Assert.assertEquals(ids(2L), idsIn(flags.findAll(filter("YNMatchCaseBoolean", "N"), Pageable.unpaged())));
    }

    @Test(expected = Filters.InvalidFilterRequest.class)
    public void throwsWhenValueDoesNotMatchCase() {
        flags.findAll(filter("YNMatchCaseBoolean", "y"), Pageable.unpaged());
    }

    @Test(expected = Filters.InvalidFilterRequest.class)
    public void throwsWhenValueDoesNotMatch() {
        flags.findAll(filter("yesNoBoolean", "maybe"), Pageable.unpaged());
    }

    private static FilterRequest filter(String filterName, String value) {
        final FilterRequest fr = new FilterRequest();
        fr.put(filterName, new String[]{value});
        return fr;
    }

    private static Set<Long> ids(Long... ids) {
        return new HashSet<>(Arrays.asList(ids));
    }

    private static Set<Long> idsIn(Page<Flag> page) {
        return page.getContent().stream().map(flag -> flag.id).collect(Collectors.toSet());
    }
}
