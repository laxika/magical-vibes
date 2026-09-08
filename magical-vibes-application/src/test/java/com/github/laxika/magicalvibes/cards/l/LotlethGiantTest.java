package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LotlethGiantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to the creature cards in its controller's graveyard")
    void etbDealsDamageForCreatureCardsInGraveyard() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        castLotlethGiant(player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("ETB counts only its controller's creature cards")
    void etbIgnoresOpponentGraveyardAndNoncreatureCards() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castLotlethGiant(player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new LotlethGiant()));
        addManaForLotlethGiant();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castLotlethGiant(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new LotlethGiant()));
        addManaForLotlethGiant();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForLotlethGiant() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
