package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrganHoarder.class, GrizzlyBears.class, Shock.class})
class OrganHoarderTest extends BaseCardTest {

    @Test
    void enteringBattlefieldCreatesLibraryChoice() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);
    }

    @Test
    void choosingOnePutsItInHandAndRestInGraveyard() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(card1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(card0, card2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void withOneCardInLibraryItAutomaticallyGoesToHand() {
        gd.playerDecks.get(player1.getId()).clear();
        Card card = new Shock();
        gd.playerDecks.get(player1.getId()).add(card);
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new OrganHoarder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
