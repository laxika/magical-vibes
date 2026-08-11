package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrimclawBatsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives Grimclaw Bats +1/+1 and costs 1 life")
    void abilityBoostsAndCostsLife() {
        Permanent bats = addReadyBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bats)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability can be activated multiple times")
    void abilityStacksBoost() {
        Permanent bats = addReadyBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bats)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bats)).isEqualTo(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough life")
    void cannotActivateWithInsufficientLife() {
        addReadyBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bats = addReadyBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bats)).isEqualTo(1);
    }

    private Permanent addReadyBats(Player player) {
        Permanent perm = new Permanent(new GrimclawBats());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
