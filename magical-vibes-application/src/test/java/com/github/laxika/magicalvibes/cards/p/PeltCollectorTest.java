package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeltCollector.class, VernadiShieldmate.class})
class PeltCollectorTest extends BaseCardTest {

    @Test
    void putsCounterOnItselfWhenLargerCreatureEnters() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());

        harness.setHand(player1, List.of(new VernadiShieldmate()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenEnteringCreatureIsNotLarger() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        pelt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new VernadiShieldmate()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void checksPowerAgainWhenEnterTriggerResolves() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());

        harness.setHand(player1, List.of(new VernadiShieldmate()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        pelt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnItselfWhenLargerCreatureDies() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());

        shieldmate.setMarkedDamage(gqs.getEffectiveToughness(gd, shieldmate));
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).contains(pelt);
        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void gainsTrampleWithThreePlusOnePlusOneCounters() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());

        assertThat(gqs.hasKeyword(gd, pelt, Keyword.TRAMPLE)).isFalse();

        pelt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(gqs.hasKeyword(gd, pelt, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void losesTrampleWhenCounterCountFallsBelowThree() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        pelt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        assertThat(gqs.hasKeyword(gd, pelt, Keyword.TRAMPLE)).isTrue();

        pelt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.hasKeyword(gd, pelt, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new PeltCollector()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(pelt -> assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void doesNotTriggerForOpponentCreatureEnteringOrDying() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new VernadiShieldmate()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent shieldmate = gd.playerBattlefields.get(player2.getId()).getFirst();
        shieldmate.setMarkedDamage(gqs.getEffectiveToughness(gd, shieldmate));
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void checksDyingCreaturesPowerAgainstCurrentSourcePowerAtResolution() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        shieldmate.setMarkedDamage(gqs.getEffectiveToughness(gd, shieldmate));
        harness.runStateBasedActions();

        pelt.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void twoEqualLargerCreaturesDyingTogetherGiveOnlyOneCounter() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        first.setMarkedDamage(gqs.getEffectiveToughness(gd, first));
        second.setMarkedDamage(gqs.getEffectiveToughness(gd, second));
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringCreatureThatDiesUsesItsPowerImmediatelyBeforeDeath() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        harness.setHand(player1, List.of(new VernadiShieldmate()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent shieldmate = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != pelt).findFirst().orElseThrow();
        shieldmate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        shieldmate.setMarkedDamage(gqs.getEffectiveToughness(gd, shieldmate));
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void negativeDyingPowerIsNotRoundedToZeroForResolutionComparison() {
        Permanent pelt = harness.addToBattlefieldAndReturn(player1, new PeltCollector());
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        pelt.setPowerModifier(-3);
        shieldmate.setPowerModifier(-3);
        shieldmate.setMarkedDamage(gqs.getEffectiveToughness(gd, shieldmate));
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        pelt.setPowerModifier(-2);
        resolveAllTriggers();

        assertThat(pelt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
