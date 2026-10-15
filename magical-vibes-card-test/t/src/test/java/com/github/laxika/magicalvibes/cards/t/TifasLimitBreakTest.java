package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hydrosurge;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TifasLimitBreak.class, GrizzlyBears.class, Hydrosurge.class, Island.class})
class TifasLimitBreakTest extends BaseCardTest {

    @Test
    @DisplayName("Somersault gives the target creature +2/+2")
    void somersaultBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(0, target, 1);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Meteor Strikes doubles the target creature's power and toughness")
    void meteorStrikesDoublesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(1, target, 3);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Final Heaven triples the target creature's power and toughness and charges its green tiered cost")
    void finalHeavenTriplesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(2, target, 8);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Tifa's Limit Break cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new TifasLimitBreak()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @ParameterizedTest
    @CsvSource({"1, 3, -6, 4", "2, 8, -9, 6"})
    @DisplayName("Doubling and tripling preserve negative power")
    void multipliesNegativePower(int mode, int totalMana, int expectedPower, int expectedToughness) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hydrosurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-3);

        cast(mode, target, totalMana);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(expectedPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(expectedToughness);
    }

    @ParameterizedTest
    @CsvSource({"0, 1, 4", "1, 3, 4", "2, 8, 6"})
    @DisplayName("Every tier can target an opponent's creature and expires at end of turn")
    void boostsOpponentsCreatureUntilEndOfTurn(int mode, int totalMana, int expectedStat) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(mode, target, totalMana);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(expectedStat);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(expectedStat);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource({"1, 3, 8", "2, 8, 12"})
    @DisplayName("Doubling and tripling use the stats when the spell resolves")
    void multipliesStatsAfterResponseResolves(int mode, int totalMana, int expectedStat) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TifasLimitBreak(), new TifasLimitBreak()));
        harness.addMana(player1, ManaColor.GREEN, mode == 2 ? 3 : 2);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - (mode == 2 ? 2 : 1));
        harness.castInstant(player1, 0, mode, target.getId());
        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(expectedStat);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(expectedStat);
    }

    @ParameterizedTest
    @CsvSource({"1, 1, 1", "2, 1, 7", "2, 2, 5"})
    @DisplayName("The additional tiered cost must be paid, including Final Heaven's second green")
    void cannotCastWithoutFullTieredCost(int mode, int greenMana, int colorlessMana) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TifasLimitBreak()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mode, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Tifa's Limit Break");
    }

    private void cast(int mode, Permanent target, int totalMana) {
        harness.setHand(player1, List.of(new TifasLimitBreak()));
        harness.addMana(player1, ManaColor.GREEN, mode == 2 ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - (mode == 2 ? 2 : 1));
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
