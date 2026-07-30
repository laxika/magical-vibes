package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuneScarredDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield offers every card in the library")
    void etbOffersAnyCard() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Rune-Scarred Demon");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
    }

    @Test
    @DisplayName("Chosen card goes to hand and leaves the library")
    void chosenCardGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search is unrestricted, so failing to find is not allowed")
    void cannotFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Empty library produces no search prompt")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Rune-Scarred Demon");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new RuneScarredDemon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GolemsHeart()));
    }
}
