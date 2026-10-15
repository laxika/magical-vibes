package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SetessanGriffin.class})
class SetessanGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Setessan Griffin +2/+2 until end of turn")
    void activationBoostsSelf() {
        Permanent griffin = addReadyGriffin(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated only once each turn")
    void activationIsLimitedToOncePerTurn() {
        addReadyGriffin(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent griffin = addReadyGriffin(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addReadyGriffin(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addActivationMana(player1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A pending activation already uses the once-per-turn allowance")
    void cannotActivateAgainBeforeResolution() {
        Permanent griffin = addReadyGriffin(player1);
        addActivationMana(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each Griffin has its own activation allowance and boosts only itself")
    void separateCopiesCanEachActivate() {
        Permanent first = addReadyGriffin(player1);
        Permanent second = addReadyGriffin(player1);
        addActivationMana(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new SetessanGriffin());
        griffin.setSummoningSick(true);
        griffin.tap();
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(griffin.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated during the opponent's turn")
    void canActivateOnOpponentsTurn() {
        Permanent griffin = addReadyGriffin(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(4);
    }

    @Test
    @DisplayName("The activation requires two green mana and a rejected attempt does not use the allowance")
    void insufficientGreenManaDoesNotUseActivationAllowance() {
        Permanent griffin = addReadyGriffin(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(4);
    }

    private Permanent addReadyGriffin(Player player) {
        return addCreatureReady(player, new SetessanGriffin());
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
