package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class MagmaSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Slivers can tap to give a target Sliver +X/+0 based on all Slivers on the battlefield")
    void boostsTargetByNumberOfSlivers() {
        addCreatureReady(player1, new MagmaSliver());
        Permanent target = addCreatureReady(player2, new BonescytheSliver());
        addCreatureReady(player2, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The temporary Magma Sliver boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MagmaSliver());
        Permanent target = addCreatureReady(player1, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("The activated ability cannot target a non-Sliver creature")
    void cannotTargetNonSliver() {
        addCreatureReady(player1, new MagmaSliver());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }
}
