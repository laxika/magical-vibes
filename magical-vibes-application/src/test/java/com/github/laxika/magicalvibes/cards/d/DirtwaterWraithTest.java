package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DirtwaterWraithTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+0")
    void resolvingAbilityBoostsPower() {
        Permanent wraith = addReadyWraith(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wraith.getEffectivePower()).isEqualTo(2);
        assertThat(wraith.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly and boosts stack")
    void canActivateMultipleTimes() {
        Permanent wraith = addReadyWraith(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wraith.getEffectivePower()).isEqualTo(4);
        assertThat(wraith.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent wraith = addReadyWraith(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wraith.getPowerModifier()).isEqualTo(0);
        assertThat(wraith.getEffectivePower()).isEqualTo(1);
        assertThat(wraith.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyWraith(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyWraith(Player player) {
        Permanent perm = new Permanent(new DirtwaterWraith());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
