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

class BagOfHoldingTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then discards and tracks the discarded card with the Bag")
    void drawsThenDiscardsAndTracksCard() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfHolding());
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        gd.playerDecks.get(player1.getId()).addFirst(drawn);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(discarded.getId()).sourcePermanentId()).isEqualTo(bag.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawn.getId()));
    }

    @Test
    @DisplayName("Sacrifice returns every card exiled with the Bag to its owner's hand")
    void sacrificeReturnsAllExiledCardsToOwnersHands() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfHolding());
        Card firstDiscard = new GrizzlyBears();
        Card secondDiscard = new GrizzlyBears();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        bag.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        bag.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(firstDiscard.getId()))
                .anyMatch(card -> card.getId().equals(secondDiscard.getId()));
        assertThat(gd.getCardsExiledByPermanent(bag.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bag of Holding");
    }
}
