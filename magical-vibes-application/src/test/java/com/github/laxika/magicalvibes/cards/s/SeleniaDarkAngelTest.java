package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeleniaDarkAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life returns Selenia to its owner's hand")
    void payLifeReturnsSelfToHand() {
        addCreatureReady(player1, new SeleniaDarkAngel());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof SeleniaDarkAngel);
    }

    @Test
    @DisplayName("Cannot activate the ability with less than 2 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new SeleniaDarkAngel());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
