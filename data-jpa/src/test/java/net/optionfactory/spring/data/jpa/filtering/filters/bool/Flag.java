package net.optionfactory.spring.data.jpa.filtering.filters.bool;

import javax.persistence.Entity;
import javax.persistence.Id;
import net.optionfactory.spring.data.jpa.filtering.filters.BooleanCompare;

@Entity
@BooleanCompare(name = "javaBoolean", property = "value")
@BooleanCompare(name = "yesNoBoolean", property = "value", trueValue = "yes", falseValue = "no")
@BooleanCompare(name = "YNMatchCaseBoolean", property = "value", trueValue = "Y", falseValue = "N", ignoreCase = false)
public class Flag {

    @Id
    public long id;
    public boolean value;
}
