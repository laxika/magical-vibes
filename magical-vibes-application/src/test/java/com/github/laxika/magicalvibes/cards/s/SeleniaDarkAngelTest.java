package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SeleniaDarkAngel.class)
class SeleniaDarkAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life returns Selenia to its owner's hand")
    void payLifeReturnsSelfToHand() {
        addCreatureReady(player1, new SeleniaDarkAngel());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "Selenia, Dark Angel");
        harness.assertInHand(player1, "Selenia, Dark Angel");
    }

    @Test
    @DisplayName("Returns to its owner's hand when controlled by an opponent")
    void returnsToOwnersHandWhenControlledByOpponent() {
        SeleniaDarkAngel selenia = new SeleniaDarkAngel();
        selenia.setOwnerId(player1.getId());
        addCreatureReady(player2, selenia);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Selenia, Dark Angel");
        harness.assertInHand(player1, "Selenia, Dark Angel");
        harness.assertNotInHand(player2, "Selenia, Dark Angel");
    }

    @Test
    @DisplayName("Cannot activate the ability with less than 2 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new SeleniaDarkAngel());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        harness.assertOnBattlefield(player1, "Selenia, Dark Angel");
    }
}
