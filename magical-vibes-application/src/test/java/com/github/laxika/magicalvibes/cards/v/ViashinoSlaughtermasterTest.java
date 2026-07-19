package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViashinoSlaughtermasterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives it +1/+1 until end of turn")
    void abilityBoostsSelf() {
        Permanent master = addMaster(player1);
        addBg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent master = addMaster(player1);
        addBg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can only be activated once each turn")
    void oncePerTurn() {
        addMaster(player1);
        addBg(player1);
        addBg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMaster(Player player) {
        Permanent perm = new Permanent(new ViashinoSlaughtermaster());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addBg(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
