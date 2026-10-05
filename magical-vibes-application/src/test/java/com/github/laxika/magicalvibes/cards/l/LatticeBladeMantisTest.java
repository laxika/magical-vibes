package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LatticeBladeMantis.class})
class LatticeBladeMantisTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two oil counters")
    void entersWithTwoOilCounters() {
        harness.setHand(player1, List.of(new LatticeBladeMantis()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mantis = findPermanent(player1, "Lattice-Blade Mantis");
        assertThat(mantis.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing an oil counter on attack untaps and boosts the Mantis")
    void removesOilCounterUntapsAndBoostsOnAttack() {
        Permanent mantis = addReadyMantis(1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(mantis.getCounterCount(CounterType.OIL)).isZero();
        assertThat(mantis.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, mantis)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mantis)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the attack ability leaves the Mantis unchanged")
    void decliningAttackAbilityDoesNothing() {
        Permanent mantis = addReadyMantis(1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(mantis.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(mantis.isTapped()).isTrue();
        assertThat(mantis.getPowerModifier()).isZero();
        assertThat(mantis.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack ability has no effect without an oil counter")
    void attackAbilityDoesNothingWithoutOilCounter() {
        Permanent mantis = addReadyMantis(0);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(mantis.getCounterCount(CounterType.OIL)).isZero();
        assertThat(mantis.isTapped()).isTrue();
        assertThat(mantis.getPowerModifier()).isZero();
        assertThat(mantis.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only one oil counter is removed and untapping does not remove the Mantis from combat")
    void removesOnlyOneCounterAndRemainsAttacking() {
        Permanent mantis = addReadyMantis(2);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();

            assertThat(mantis.getCounterCount(CounterType.OIL)).isEqualTo(1);
            assertThat(mantis.isTapped()).isFalse();
            assertThat(mantis.isAttacking()).isTrue();
            assertThat(gqs.getEffectivePower(gd, mantis)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, mantis)).isEqualTo(4);
        });

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, mantis)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mantis)).isEqualTo(3);
        assertThat(mantis.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Losing the last oil counter before resolution prevents untapping and boosting")
    void counterRemovedBeforeResolutionPreventsBonus() {
        Permanent mantis = addReadyMantis(1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            mantis.setCounterCount(CounterType.OIL, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();

            assertThat(mantis.getCounterCount(CounterType.OIL)).isZero();
            assertThat(mantis.isTapped()).isTrue();
            assertThat(gqs.getEffectivePower(gd, mantis)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, mantis)).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("An oil counter gained after attacking can be removed when the ability resolves")
    void counterAddedBeforeResolutionCanPayForBonus() {
        Permanent mantis = addReadyMantis(0);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            mantis.setCounterCount(CounterType.OIL, 1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();

            assertThat(mantis.getCounterCount(CounterType.OIL)).isZero();
            assertThat(mantis.isTapped()).isFalse();
            assertThat(gqs.getEffectivePower(gd, mantis)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, mantis)).isEqualTo(4);
        });
    }

    private Permanent addReadyMantis(int oilCounters) {
        Permanent mantis = addCreatureReady(player1, new LatticeBladeMantis());
        mantis.setCounterCount(CounterType.OIL, oilCounters);
        return mantis;
    }
}
