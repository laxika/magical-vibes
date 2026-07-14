package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FigureOfDestinyTest extends BaseCardTest {

    private Permanent addFigure() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new FigureOfDestiny());
        return harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
    }

    private void resetPriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("First ability sets base power and toughness to 2/2")
    void firstAbilityMakesTwoByTwo() {
        Permanent figure = addFigure();
        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Full level-up chain reaches 8/8 with flying and first strike")
    void fullChainToAvatar() {
        Permanent figure = addFigure();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(2);

        resetPriority();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(4);

        resetPriority();
        harness.addMana(player1, ManaColor.RED, 6);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, figure, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Higher ability does nothing without the required subtype")
    void gatedAbilityDoesNothingWithoutPrerequisite() {
        Permanent figure = addFigure();

        // Activate the 4/4 ability while the creature is not yet a Spirit: intervening "if" fails.
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, figure)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, figure)).isEqualTo(1);
    }
}
