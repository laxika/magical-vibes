package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImplementOfFerocity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RidgescaleTusker.class, GrizzlyBears.class, ImplementOfFerocity.class})
class RidgescaleTuskerTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability puts a +1/+1 counter on each other creature you control")
    void putsCountersOnOtherCreaturesYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RidgescaleTusker()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent tusker = findPermanent(player1, "Ridgescale Tusker");
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(tusker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters are added to every other creature, including another Tusker, but not artifacts")
    void putsCountersOnAllOtherCreaturesAndPreservesExistingCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RidgescaleTusker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RidgescaleTusker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ImplementOfFerocity());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        Permanent source = harness.enterBattlefieldAndReturn(player1, new RidgescaleTusker());
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The ability resolves with no other creatures and does not put a counter on itself")
    void resolvesWithNoOtherCreatures() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new RidgescaleTusker());

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The ability uses the creatures present when it resolves and survives its source leaving")
    void usesBattlefieldAtResolutionEvenAfterSourceLeaves() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new RidgescaleTusker());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new RidgescaleTusker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RidgescaleTusker());

        resolveAllTriggers();

        assertThat(laterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
