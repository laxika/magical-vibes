package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ExoticOrchard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CAMP.class, ExoticOrchard.class, Forest.class, GrizzlyBears.class, Mountain.class})
class CAMPTest extends BaseCardTest {

    @Test
    @DisplayName("A matching mana color puts a counter on a chosen creature and creates a Junk")
    void matchingManaColorCountersCreatureAndCreatesJunk() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        camp.setAttachedTo(forest.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.tapPermanent(player1, 1);
        chooseTarget(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    @DisplayName("A nonmatching mana color still puts the counter on the creature")
    void nonmatchingManaColorDoesNotCreateJunk() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        camp.setAttachedTo(mountain.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.tapPermanent(player1, 1);
        chooseTarget(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    @DisplayName("The color chosen for an any-color land is used by the trigger")
    void chosenAnyColorManaIsUsedByTrigger() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent orchard = harness.addToBattlefieldAndReturn(player1, new ExoticOrchard());
        camp.setAttachedTo(orchard.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");
        chooseTarget(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    private void chooseTarget(Permanent target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
