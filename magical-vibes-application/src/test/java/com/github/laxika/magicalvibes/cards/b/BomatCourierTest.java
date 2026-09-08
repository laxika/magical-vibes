package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BomatCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card of its controller's library face down when it attacks")
    void attacksExilesTopCardFaceDown() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        Card topCard = new GrizzlyBears();
        Card remainingCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, remainingCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.getCardsExiledByPermanent(courier.getId()))
                .extracting(Card::getId)
                .containsExactly(topCard.getId());
        assertThat(gd.getExiledWithPermanentEntries(courier.getId(), courier.getCard().getId()))
                .allMatch(entry -> entry.faceDown());
    }

    @Test
    @DisplayName("Returns its exiled cards to their owners' hands after paying the ability's costs")
    void sacrificeReturnsExiledCardsAndDiscardsHand() {
        Permanent courier = addCreatureReady(player1, new BomatCourier());
        Card exiledCard = new GrizzlyBears();
        Card discardedCard = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiledCard, courier.getId());
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiledCard);
        assertThat(gd.getCardsExiledByPermanent(courier.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(courier.getCard());
    }
}
