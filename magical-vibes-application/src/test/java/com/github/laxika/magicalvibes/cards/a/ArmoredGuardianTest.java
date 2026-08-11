package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmoredGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gains protection from the chosen color")
    void grantsProtectionFromChosenColor() {
        addReadyGuardian(player1);
        Permanent bears = addReadyBears(player1);
        addWhiteProtectionMana();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Protection from the chosen color wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        addReadyGuardian(player1);
        Permanent bears = addReadyBears(player1);
        addWhiteProtectionMana();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("The protection ability cannot target an opponent's creature")
    void protectionAbilityCannotTargetOpponentCreature() {
        addReadyGuardian(player1);
        Permanent enemyBears = addReadyBears(player2);
        addWhiteProtectionMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enemyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second ability grants this creature shroud until end of turn")
    void grantsShroudUntilEndOfTurn() {
        Permanent guardian = addReadyGuardian(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.SHROUD)).isFalse();
    }

    private void addWhiteProtectionMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyGuardian(Player player) {
        Permanent guardian = harness.addToBattlefieldAndReturn(player, new ArmoredGuardian());
        guardian.setSummoningSick(false);
        return guardian;
    }

    private Permanent addReadyBears(Player player) {
        Permanent bears = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bears.setSummoningSick(false);
        return bears;
    }
}
