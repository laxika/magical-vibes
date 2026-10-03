package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.SealOfFire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestialAncient.class, MistralCharger.class, SealOfFire.class})
class CelestialAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Each Celestial Ancient triggers independently for an enchantment spell")
    void multipleAncientsEachAddCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CelestialAncient());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CelestialAncient());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());

        harness.castFromHand(player1, new SealOfFire(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An enchantment entering without being cast does not trigger Celestial Ancient")
    void enchantmentEnteringWithoutCastDoesNotAddCounters() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new CelestialAncient());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());

        harness.enterBattlefieldAndReturn(player1, new SealOfFire());

        assertThat(gd.stack).isEmpty();
        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger affects creatures present when it resolves")
    void creaturesEnteringBeforeTriggerResolvesReceiveCounters() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new CelestialAncient());

        harness.castFromHand(player1, new SealOfFire(), "{R}");
        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an enchantment puts a +1/+1 counter on each creature you control")
    void castingEnchantmentAddsCountersToOwnCreatures() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new CelestialAncient());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new SealOfFire());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MistralCharger());

        harness.castFromHand(player1, new SealOfFire(), "{R}");
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownEnchantment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a non-enchantment spell does not put counters on creatures")
    void castingNonEnchantmentDoesNotAddCounters() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new CelestialAncient());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent casting an enchantment does not put counters on your creatures")
    void opponentCastingEnchantmentDoesNotAddCounters() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new CelestialAncient());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MistralCharger());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new SealOfFire(), "{R}");
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
