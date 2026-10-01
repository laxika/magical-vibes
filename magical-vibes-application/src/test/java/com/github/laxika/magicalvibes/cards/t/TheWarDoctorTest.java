package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SlipOutTheBack;
import com.github.laxika.magicalvibes.cards.s.SpikefieldCave;
import com.github.laxika.magicalvibes.cards.s.SpikefieldHazard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWarDoctor.class, GrizzlyBears.class, SlipOutTheBack.class,
        SpikefieldHazard.class, SpikefieldCave.class})
class TheWarDoctorTest extends BaseCardTest {

    @Test
    void putsTimeCounterOnAnotherPermanentPhasingOut() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SlipOutTheBack()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void putsTimeCounterWhenACardIsExiled() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new SpikefieldHazard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void attacksAndDealsDamageEqualToItsTimeCounters() {
        Permanent doctor = addCreatureReady(player1, new TheWarDoctor());
        doctor.setCounterCount(CounterType.TIME, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }
}
