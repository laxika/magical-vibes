package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianBattlefliesTest extends BaseCardTest {

    @Test
    @DisplayName("Pump ability grants +1/+0 until end of turn")
    void pumpAbilityGrantsBoost() {
        Permanent battleflies = addReadyBattleflies(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, battleflies)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, battleflies)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pump ability can be activated twice but not three times each turn")
    void pumpAbilityCanBeActivatedTwiceEachTurn() {
        Permanent battleflies = addReadyBattleflies(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, battleflies)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 2 times each turn");
    }

    @Test
    @DisplayName("Pump boost wears off at end of turn")
    void pumpBoostWearsOff() {
        Permanent battleflies = addReadyBattleflies(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, battleflies)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, battleflies)).isEqualTo(0);
    }

    private Permanent addReadyBattleflies(Player player) {
        Permanent perm = new Permanent(new PhyrexianBattleflies());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
