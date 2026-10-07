package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PumpkinBombs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwistedSpiderClone.class, GrizzlyBears.class, PumpkinBombs.class})
class TwistedSpiderCloneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on each controlled creature that already has one")
    void etbPutsCountersOnExistingCounterBearers() {
        Permanent withCounter = addCreatureReady(player1, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent withoutCounter = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new TwistedSpiderClone()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(withCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(withoutCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Eligibility is checked when the trigger resolves, including the entering creature")
    void checksCountersAtResolutionAndIncludesItself() {
        Permanent gainsCounter = addCreatureReady(player1, new TwistedSpiderClone());
        Permanent losesCounter = addCreatureReady(player1, new TwistedSpiderClone());
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, List.of(new TwistedSpiderClone()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        gainsCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(gainsCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(losesCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreatures and creatures with only other counters are excluded")
    void excludesNoncreaturesAndOtherCounterTypes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PumpkinBombs());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent otherCounter = addCreatureReady(player1, new TwistedSpiderClone());
        otherCounter.setCounterCount(CounterType.REACH, 1);
        Permanent severalCounters = addCreatureReady(player1, new TwistedSpiderClone());
        severalCounters.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new TwistedSpiderClone()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCounter.getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(severalCounters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()).getLast()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger still resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent recipient = addCreatureReady(player1, new TwistedSpiderClone());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new TwistedSpiderClone()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        Permanent source = gd.playerBattlefields.get(player1.getId()).getLast();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
