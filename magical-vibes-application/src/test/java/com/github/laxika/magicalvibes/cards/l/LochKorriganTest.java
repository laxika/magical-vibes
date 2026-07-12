package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LochKorriganTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+1, payable with blue mana")
    void resolvingBoostsWithBlue() {
        addKorriganReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent korrigan = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(korrigan.getEffectivePower()).isEqualTo(2);
        assertThat(korrigan.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability is also payable with black mana (hybrid cost)")
    void payableWithBlack() {
        addKorriganReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent korrigan = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(korrigan.getEffectivePower()).isEqualTo(2);
        assertThat(korrigan.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate multiple times, stacking the boost")
    void stacksMultipleActivations() {
        addKorriganReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent korrigan = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(korrigan.getEffectivePower()).isEqualTo(3);
        assertThat(korrigan.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addKorriganReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent korrigan = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(korrigan.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(korrigan.getEffectivePower()).isEqualTo(1);
        assertThat(korrigan.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addKorriganReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addKorriganReady(Player player) {
        LochKorrigan card = new LochKorrigan();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
