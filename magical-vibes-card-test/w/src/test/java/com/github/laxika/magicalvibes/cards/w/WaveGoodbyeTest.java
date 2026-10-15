package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaveGoodbye.class, GrizzlyBears.class, Island.class})
class WaveGoodbyeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creatures without +1/+1 counters and leaves other permanents alone")
    void returnsCreaturesWithoutPlusOneCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        protectedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castFromHand(player1, new WaveGoodbye(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(opposingCreature);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Wave Goodbye");
    }

    @Test
    @DisplayName("Other counter types do not protect creatures")
    void returnsCreaturesWithOtherCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.castFromHand(player1, new WaveGoodbye(), "{2}{U}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Checks counters at resolution rather than when cast")
    void checksCountersAtResolution() {
        Permanent gainsCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent losesCounter = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new WaveGoodbye(), "{2}{U}{U}");
        gainsCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gainsCounter);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a creature to its owner even under another player's control")
    void returnsStolenCreatureToOwner() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.stolenCreatures.put(creature.getId(), player1.getId());

        harness.castFromHand(player1, new WaveGoodbye(), "{2}{U}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves normally when every creature has a +1/+1 counter")
    void resolvesWithNoEligibleCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castFromHand(player1, new WaveGoodbye(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Wave Goodbye");
        assertThat(gd.stack).isEmpty();
    }
}
