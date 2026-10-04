package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpansionAlgorithm.class, GrizzlyBears.class})
class ExpansionAlgorithmTest extends BaseCardTest {

    @Test
    void proliferatesXTimes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new ExpansionAlgorithm()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=2: {2}{U}{U}

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void withXZeroDoesNotProliferate() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new ExpansionAlgorithm()));
        harness.addMana(player1, ManaColor.BLUE, 2); // X=0: {0}{U}{U}

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void energyOnlyPlayerRemainsEligibleForEveryProliferation() {
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new ExpansionAlgorithm()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void experienceOnlyPlayerRemainsEligibleForEveryProliferation() {
        gd.playerExperienceCounters.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new ExpansionAlgorithm()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void canChooseDifferentObjectsAndDeclineAnIteration() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        first.setCounterCount(CounterType.CHARGE, 2);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new ExpansionAlgorithm()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 3);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), player2.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(first.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        harness.assertInGraveyard(player1, "Expansion Algorithm");
    }

    @Test
    void resolvesWithNoCountersToProliferate() {
        harness.setHand(player1, List.of(new ExpansionAlgorithm()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertInGraveyard(player1, "Expansion Algorithm");
        assertThat(gd.stack).isEmpty();
    }
}
