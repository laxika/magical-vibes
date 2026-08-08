package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KraulWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives +3/+3")
    void abilityBoostsSelf() {
        Permanent warrior = addReadyKraulWarrior(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(5);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly")
    void abilityStacks() {
        Permanent warrior = addReadyKraulWarrior(player1);
        harness.addMana(player1, ManaColor.GREEN, 12);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(8);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent warrior = addReadyKraulWarrior(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warrior.getPowerModifier()).isEqualTo(0);
        assertThat(warrior.getToughnessModifier()).isEqualTo(0);
        assertThat(warrior.getEffectivePower()).isEqualTo(2);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyKraulWarrior(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyKraulWarrior(Player player) {
        Permanent perm = new Permanent(new KraulWarrior());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
