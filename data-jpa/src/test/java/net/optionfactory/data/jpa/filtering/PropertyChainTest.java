package net.optionfactory.data.jpa.filtering;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import net.optionfactory.data.jpa.HibernateTestConfig;
import net.optionfactory.data.jpa.filtering.filters.TextCompare;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionTemplate;

public class PropertyChainTest {

    private ActivitiesRepository activities;
    private PerformersRepository performers;
    private AppointmentsRepository appointments;
    private TransactionTemplate tx;

    @Before
    public void setup() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(HibernateTestConfig.class);
        ctx.refresh();
        this.activities = ctx.getBean(ActivitiesRepository.class);
        this.performers = ctx.getBean(PerformersRepository.class);
        this.appointments = ctx.getBean(AppointmentsRepository.class);
        this.tx = ctx.getBean(TransactionTemplate.class);
        tx.execute(txs -> {
            activities.deleteAll();
            performers.deleteAll();
            appointments.deleteAll();

            final Activity swimming = activities.save(activity(1, "swimming", "Swimming!", Activity.Season.SUMMER));
            final Activity skying = activities.save(activity(2, "skying", "Skying!", Activity.Season.WINTER));

            final Performer pietro = performers.save(performer(1, "pietro"));
            final Performer paolo = performers.save(performer(2, "paolo"));
            final Performer pietreppaolo = performers.save(performer(3, "pietreppaolo"));

            appointments.saveAll(Arrays.asList(
                    appointment(1, Instant.EPOCH, LocalDate.parse("2019-01-10"), skying, pietro, Appointment.Status.CONFIRMED),
                    appointment(2, Instant.EPOCH, LocalDate.parse("2019-01-10"), skying, paolo, Appointment.Status.CONFIRMED),
                    appointment(3, Instant.EPOCH, LocalDate.parse("2019-02-20"), skying, pietreppaolo, Appointment.Status.CANCELED),
                    appointment(4, Instant.EPOCH, LocalDate.parse("2019-06-01"), swimming, pietro, Appointment.Status.PENDING),
                    appointment(5, Instant.EPOCH, LocalDate.parse("2019-06-03"), swimming, paolo, Appointment.Status.PENDING),
                    appointment(6, Instant.EPOCH, LocalDate.parse("2019-07-07"), swimming, pietreppaolo, Appointment.Status.CONFIRMED)
            ));

            return null;
        });
    }

    @Test
    public void canFilterByComparingPerformerName() {
        final FilterRequest fr = new FilterRequest();
        fr.put("performerName", new String[]{
            TextCompare.Operator.EQUALS.toString(),
            TextCompare.Mode.CASE_SENSITIVE.toString(),
            "pietro"
        });
        final Pageable pr = Pageable.unpaged();
        final Page<Appointment> page = tx.execute(txs -> appointments.findAll(fr, pr));
        Assert.assertEquals(new HashSet<>(Arrays.asList(1L, 4L)), page.getContent().stream().map(a -> a.id).collect(Collectors.toSet()));
    }

    @Test
    public void canFilterByEmptyEnum() {
        final FilterRequest fr = new FilterRequest();
        fr.put("status", new String[0]);
        final Pageable pr = Pageable.unpaged();
        final Page<Appointment> page = tx.execute(txs -> appointments.findAll(fr, pr));
        Assert.assertTrue(page.isEmpty());
    }

    @Test
    public void canFilterByStatusInEnum() {
        final FilterRequest fr = new FilterRequest();
        fr.put("status", new String[]{Appointment.Status.CONFIRMED.name()});
        final Pageable pr = Pageable.unpaged();
        final Page<Appointment> page = tx.execute(txs -> appointments.findAll(fr, pr));
        Assert.assertEquals(new HashSet<>(Arrays.asList(1L, 2L, 6L)), page.getContent().stream().map(a -> a.id).collect(Collectors.toSet()));
    }

    @Test
    public void canFilterByActivitySeasonInEnum() {
        final FilterRequest fr = new FilterRequest();
        fr.put("season", new String[]{Activity.Season.SUMMER.name()});
        final Pageable pr = Pageable.unpaged();
        final Page<Appointment> page = tx.execute(txs -> appointments.findAll(fr, pr));
        Assert.assertEquals(new HashSet<>(Arrays.asList(4L, 5L, 6L)), page.getContent().stream().map(a -> a.id).collect(Collectors.toSet()));
    }

    private static Activity activity(long id, String name, String description, Activity.Season season) {
        final Activity activity = new Activity();
        activity.id = id;
        activity.name = name;
        activity.description = description;
        activity.season = season;
        return activity;
    }

    private static Performer performer(long id, String name) {
        final Performer performer = new Performer();
        performer.id = id;
        performer.name = name;
        return performer;
    }

    private static Appointment appointment(long id, Instant created, LocalDate date, Activity activity, Performer performer, Appointment.Status status) {
        final Appointment appointment = new Appointment();
        appointment.id = id;
        appointment.created = created;
        appointment.date = date;
        appointment.activity = activity;
        appointment.performer = performer;
        appointment.status = status;
        return appointment;
    }
}