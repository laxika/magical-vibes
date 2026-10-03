package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Breakthrough.class, CabalCoffers.class})
class BreakthroughTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards, then discards down to X cards")
    void drawsFourThenDiscardsDownToX() {
        harness.setLibrary(player1, List.of(
                new CabalCoffers(), new CabalCoffers(), new CabalCoffers(), new CabalCoffers()));
        harness.setHand(player1, List.of(new Breakthrough(), new CabalCoffers(), new CabalCoffers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("X zero discards the whole hand after drawing")
    void xZeroDiscardsWholeHand() {
        harness.setLibrary(player1, List.of(
                new CabalCoffers(), new CabalCoffers(), new CabalCoffers(), new CabalCoffers()));
        harness.setHand(player1, List.of(new Breakthrough(), new CabalCoffers(), new CabalCoffers()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        for (int i = 0; i < 6; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Does not discard when X exceeds the post-draw hand size")
    void xExceedsPostDrawHandSize() {
        harness.setLibrary(player1, List.of(
                new CabalCoffers(), new CabalCoffers(), new CabalCoffers(), new CabalCoffers()));
        harness.setHand(player1, List.of(new Breakthrough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 5);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can keep both an existing hand card and a newly drawn card")
    void choosesWhichCardsToKeep() {
        CabalCoffers existingCard = new CabalCoffers();
        CabalCoffers drawnCard = new CabalCoffers();
        Breakthrough spell = new Breakthrough();
        List<CabalCoffers> library = List.of(
                drawnCard, new CabalCoffers(), new CabalCoffers(), new CabalCoffers());
        CabalCoffers opponentCard = new CabalCoffers();
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(spell, existingCard));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(
                existingCard, library.get(0), library.get(1), library.get(2), library.get(3));
        harness.handleCardChosen(player1, 2);
        harness.handleCardChosen(player1, 2);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(existingCard, drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(
                spell, library.get(1), library.get(2), library.get(3));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Keeps every card without a discard choice when X equals the post-draw hand size")
    void xEqualsPostDrawHandSize() {
        List<CabalCoffers> library = List.of(
                new CabalCoffers(), new CabalCoffers(), new CabalCoffers(), new CabalCoffers());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new Breakthrough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
