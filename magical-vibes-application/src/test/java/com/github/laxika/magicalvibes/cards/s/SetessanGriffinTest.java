package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .hasMessageContaining("no more than 1 times each turn");
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

    private Permanent addReadyGriffin(Player player) {
        Permanent perm = new Permanent(new SetessanGriffin());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
