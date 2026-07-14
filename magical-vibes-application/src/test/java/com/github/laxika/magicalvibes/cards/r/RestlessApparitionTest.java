package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestlessApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +3/+3, payable with white mana")
    void resolvingBoostsWithWhite() {
        Permanent apparition = addApparitionReady(player1);
        int basePower = apparition.getEffectivePower();
        int baseToughness = apparition.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower + 3);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Ability is also payable with black mana (hybrid cost)")
    void payableWithBlack() {
        Permanent apparition = addApparitionReady(player1);
        int basePower = apparition.getEffectivePower();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower + 3);
    }

    @Test
    @DisplayName("Can activate multiple times, stacking the boost")
    void stacksMultipleActivations() {
        Permanent apparition = addApparitionReady(player1);
        int basePower = apparition.getEffectivePower();
        int baseToughness = apparition.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower + 6);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(baseToughness + 6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent apparition = addApparitionReady(player1);
        int basePower = apparition.getEffectivePower();
        int baseToughness = apparition.getEffectiveToughness();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower + 3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(apparition.getEffectivePower()).isEqualTo(basePower);
        assertThat(apparition.getEffectiveToughness()).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addApparitionReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addApparitionReady(Player player) {
        RestlessApparition card = new RestlessApparition();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
