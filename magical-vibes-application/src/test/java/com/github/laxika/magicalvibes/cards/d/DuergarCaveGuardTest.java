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

class DuergarCaveGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+0, payable with red mana")
    void resolvingBoostsWithRed() {
        addGuardReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent guard = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(guard.getEffectivePower()).isEqualTo(2);
        assertThat(guard.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability is also payable with white mana (hybrid cost)")
    void payableWithWhite() {
        addGuardReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent guard = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(guard.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate multiple times, stacking the boost")
    void stacksMultipleActivations() {
        addGuardReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent guard = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(guard.getEffectivePower()).isEqualTo(3);
        assertThat(guard.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addGuardReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent guard = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(guard.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(guard.getEffectivePower()).isEqualTo(1);
        assertThat(guard.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addGuardReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addGuardReady(Player player) {
        DuergarCaveGuard card = new DuergarCaveGuard();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
