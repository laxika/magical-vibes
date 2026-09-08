package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevouringHellion.class, GarrukWildspeaker.class, GrizzlyBears.class, Mountain.class})
class DevouringHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature and planeswalker gives two counters per permanent")
    void sacrificingCreatureAndPlaneswalkerAddsTwoCountersEach() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent garruk = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        garruk.setCounterCount(CounterType.LOYALTY, 3);
        harness.addToBattlefieldAndReturn(player1, new Mountain());

        castHellion();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), garruk.getId()));

        assertThat(findPermanent(player1, "Devouring Hellion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player1, "Garruk Wildspeaker")).isZero();
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing zero permanents leaves the Hellion without counters")
    void choosingZeroPermanentsLeavesItWithoutCounters() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHellion();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(findPermanent(player1, "Devouring Hellion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prompt or sacrifice a noncreature nonplaneswalker")
    void doesNotSacrificeNonmatchingPermanent() {
        harness.addToBattlefieldAndReturn(player1, new Mountain());

        castHellion();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Devouring Hellion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
    }

    private void castHellion() {
        harness.setHand(player1, new ArrayList<>(List.of(new DevouringHellion())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
    }
}
