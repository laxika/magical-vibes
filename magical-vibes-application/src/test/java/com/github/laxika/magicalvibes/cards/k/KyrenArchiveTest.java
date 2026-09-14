package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyrenArchive.class, KyrenToy.class})
class KyrenArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("May exile the top card of its controller's library face down during upkeep")
    void upkeepExilesTopCardFaceDown() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new KyrenArchive());
        Card topCard = new KyrenToy();
        Card remainingCard = new KyrenToy();
        harness.setLibrary(player1, List.of(topCard, remainingCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.getCardsExiledByPermanent(archive.getId()))
                .extracting(Card::getId)
                .containsExactly(topCard.getId());
        assertThat(gd.getExiledWithPermanentEntries(archive.getId(), archive.getCard().getId()))
                .allMatch(entry -> entry.faceDown());
    }

    @Test
    @DisplayName("May decline to exile the top card during upkeep")
    void upkeepMayDeclineToExileTopCard() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new KyrenArchive());
        Card topCard = new KyrenToy();
        Card remainingCard = new KyrenToy();
        harness.setLibrary(player1, List.of(topCard, remainingCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, remainingCard);
        assertThat(gd.getCardsExiledByPermanent(archive.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when its controller's library is empty")
    void upkeepWithEmptyLibraryDoesNotExileCard() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new KyrenArchive());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(archive.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void upkeepDoesNotTriggerDuringOpponentUpkeep() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new KyrenArchive());
        Card topCard = new KyrenToy();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getCardsExiledByPermanent(archive.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discards its controller's hand and returns all exiled cards when sacrificed")
    void sacrificeReturnsExiledCardsAndDiscardsHand() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new KyrenArchive());
        Card ownExiledCard = new KyrenToy();
        Card opponentExiledCard = new KyrenToy();
        Card discardedCard = new KyrenToy();
        gd.addToExile(player1.getId(), ownExiledCard, archive.getId());
        gd.addToExile(player2.getId(), opponentExiledCard, archive.getId());
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownExiledCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentExiledCard);
        assertThat(gd.getCardsExiledByPermanent(archive.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(archive.getCard());
    }
}
