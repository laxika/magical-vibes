package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlisteringDieflynTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+0, payable with red mana")
    void resolvingBoostsWithRed() {
        addDieflynReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent dieflyn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dieflyn.getEffectivePower()).isEqualTo(1);
        assertThat(dieflyn.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability is also payable with black mana (hybrid cost)")
    void payableWithBlack() {
        addDieflynReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent dieflyn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dieflyn.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate multiple times, stacking the boost")
    void stacksMultipleActivations() {
        addDieflynReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent dieflyn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dieflyn.getEffectivePower()).isEqualTo(2);
        assertThat(dieflyn.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addDieflynReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent dieflyn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dieflyn.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dieflyn.getEffectivePower()).isEqualTo(0);
        assertThat(dieflyn.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addDieflynReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addDieflynReady(Player player) {
        BlisteringDieflyn card = new BlisteringDieflyn();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
