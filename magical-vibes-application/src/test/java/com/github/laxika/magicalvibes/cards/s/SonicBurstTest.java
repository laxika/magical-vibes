package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SonicBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a random card as a cost and deals 4 damage to any target")
    void discardsRandomCardAndDealsDamage() {
        harness.setHand(player1, List.of(new SonicBurst(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Cannot be cast when there is no other card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new SonicBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card at random");
    }
}
