package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorldlyTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only creature cards from the library")
    void offersOnlyCreatures() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Choosing a creature puts it on top of the library")
    void choosingCreaturePutsOnTop() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        gs.handleLibraryCardChosen(gd, player1, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
    }

    @Test
    @DisplayName("Failing to find is allowed")
    void failToFindIsAllowed() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        gs.handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No interaction when the library has no creatures")
    void noCreaturesNoInteraction() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new DiabolicTutor(), new Island()));

        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void cast() {
        harness.setHand(player1, List.of(new WorldlyTutor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new DiabolicTutor(), new Island()));
    }
}
