package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanTutor.class, GrizzlyBears.class, Island.class})
class SylvanTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only creature cards from the library")
    void offersOnlyCreatures() {
        setupLibrary();
        castAndResolve();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Choosing a creature reveals it before putting it on top")
    void choosingCreatureRevealsIt() {
        setupLibrary();
        castAndResolve();

        GameData gd = harness.getGameData();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals " + chosenName)
                        && entry.contains("puts it on top of their library")
                        && entry.contains("Library is shuffled."));
    }

    @Test
    @DisplayName("Choosing a creature puts it on top of the library")
    void choosingCreaturePutsOnTop() {
        setupLibrary();
        castAndResolve();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
    }

    @Test
    @DisplayName("Failing to find is allowed")
    void failToFindIsAllowed() {
        setupLibrary();
        castAndResolve();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No interaction when the library has no creatures")
    void noCreaturesNoInteraction() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        castAndResolve();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("An empty library still completes the spell")
    void emptyLibraryCompletesSpell() {
        harness.setLibrary(player1, List.of());

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SylvanTutor);
    }

    @Test
    @DisplayName("The only card in the library can be found and remains on top")
    void findsOnlyCardInLibrary() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        castAndResolve();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing the second creature preserves every card and leaves the opponent's library alone")
    void choosesSecondCreatureFromOwnLibrary() {
        Card firstCreature = new GrizzlyBears();
        Card chosenCreature = new GrizzlyBears();
        Card land = new Island();
        Card opponentCreature = new GrizzlyBears();
        Card opponentLand = new Island();
        harness.setLibrary(player1, List.of(firstCreature, land, chosenCreature));
        harness.setLibrary(player2, List.of(opponentCreature, opponentLand));

        castAndResolve();
        harness.handleCardChosen(player1, 1);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.getFirst()).isSameAs(chosenCreature);
        assertThat(library).containsExactlyInAnyOrder(firstCreature, land, chosenCreature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCreature, opponentLand);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SylvanTutor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island(), new Island()));
    }
}
