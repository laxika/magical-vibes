package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FearlessPupTest extends BaseCardTest {

    @Test
    @DisplayName("Boast gives Fearless Pup +2/+0 until end of turn")
    void boastBoostsFearlessPup() {
        Permanent pup = addCreatureReady(player1, new FearlessPup());
        pup.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pup.getEffectivePower()).isEqualTo(3);
        assertThat(pup.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fearless Pup's boast requires it to have attacked this turn")
    void boastRequiresAttack() {
        addCreatureReady(player1, new FearlessPup());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Fearless Pup's boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent pup = addCreatureReady(player1, new FearlessPup());
        pup.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }
}
