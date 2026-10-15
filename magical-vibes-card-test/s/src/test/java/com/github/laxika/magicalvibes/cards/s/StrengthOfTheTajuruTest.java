package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrengthOfTheTajuru.class, ArborElf.class, SnappingCreeper.class, EverflowingChalice.class})
class StrengthOfTheTajuruTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, puts X counters on one target creature")
    void putsXCountersOnOneTargetWithoutMultikicker() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        castWithTargets(1, List.of(bears.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each multikicker payment adds another target and each target gets X counters")
    void multikickerAddsTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new SnappingCreeper());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new ArborElf());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        castWithTargets(3, List.of(bears.getId(), giant.getId(), third.getId()), List.of("{1}", "{1}"));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(6);
        assertThat(third.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target more creatures than the number of targets bought by multikicker")
    void cannotTargetMoreCreaturesThanBought() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new SnappingCreeper());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        List<UUID> targets = List.of(bears.getId(), giant.getId());
        assertThatThrownBy(() -> castWithTargets(1, targets, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EverflowingChalice());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> castWithTargets(1, List.of(artifact.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void xIsIndependentOfKickerCount() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArborElf());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        castWithTargets(5, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(5);
    }

    @Test
    void canKickWithXSmallerThanTargetCount() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArborElf());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SnappingCreeper());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        castWithTargets(1, List.of(first.getId(), second.getId()), List.of("{1}"));
        harness.passBothPriorities();

        assertThat(first.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(second.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void zeroXStillTargetsACreatureButAddsNoCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        castWithTargets(0, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        assertThat(creature.getPlusOnePlusOneCounters()).isZero();
        harness.assertInGraveyard(player1, "Strength of the Tajuru");
    }

    @Test
    void mustChooseOneAdditionalTargetForEveryKick() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> castWithTargets(2, List.of(creature.getId()), List.of("{1}")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotOmitTheBaseTarget() {
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> castWithTargets(1, List.of(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eachTargetMustBeDifferent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> castWithTargets(2,
                List.of(creature.getId(), creature.getId()), List.of("{1}")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvesForRemainingLegalTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArborElf());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SnappingCreeper());
        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        castWithTargets(2, List.of(first.getId(), second.getId()), List.of("{1}"));
        gd.playerBattlefields.get(player2.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(first.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(second.getPlusOnePlusOneCounters()).isZero();
    }

    private void castWithTargets(int xValue, List<UUID> targets, List<String> repeatedCosts) {
        if (repeatedCosts.isEmpty()) {
            harness.castInstantForX(player1, 0, xValue, targets);
            return;
        }
        gs.playCard(gd, player1, 0, xValue, null, null, targets, List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                repeatedCosts, false);
    }
}
