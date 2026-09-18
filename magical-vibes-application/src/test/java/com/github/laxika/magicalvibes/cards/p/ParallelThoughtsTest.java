package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParallelThoughts.class, ScornfulEgotist.class, AvenMindcensor.class})
class ParallelThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven cards exiled face down in a source-tracked pile")
    void etbExilesSevenCardsIntoPile() {
        harness.setLibrary(player1, List.of(
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(),
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist()));
        harness.setHand(player1, List.of(new ParallelThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        for (int i = 0; i < 7; i++) {
            harness.handleCardChosen(player1, 0);
        }

        UUID sourcePermanentId = harness.getPermanentId(player1, "Parallel Thoughts");
        assertThat(gd.getCardsExiledByPermanent(sourcePermanentId)).hasSize(7);
        assertThat(gd.exiledCards).filteredOn(e -> sourcePermanentId.equals(e.sourcePermanentId()))
                .allMatch(e -> e.faceDown());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Exiles every available card when the library has fewer than seven cards")
    void etbExilesEveryAvailableCardFromShortLibrary() {
        List<Card> library = List.of(new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ParallelThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        for (int i = 0; i < library.size(); i++) {
            harness.handleCardChosen(player1, 0);
        }

        UUID sourcePermanentId = harness.getPermanentId(player1, "Parallel Thoughts");
        assertThat(gd.getCardsExiledByPermanent(sourcePermanentId)).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(library.stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Requires seven cards when the library contains at least seven cards")
    void cannotFailToFindAfterStartingTheSevenCardSearch() {
        harness.setLibrary(player1, List.of(
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(),
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist()));
        harness.setHand(player1, List.of(new ParallelThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot fail to find with an unrestricted search");
    }

    @Test
    @DisplayName("Applies an opponent's top-four search restriction to every pick")
    void appliesOpponentSearchRestrictionToSubsequentPicks() {
        List<Card> library = List.of(
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(),
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist());
        harness.addToBattlefield(player2, new AvenMindcensor());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ParallelThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        var initialSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(initialSearch).isNotNull();
        assertThat(initialSearch.params().cards())
                .extracting(Card::getId)
                .containsExactly(library.get(0).getId(), library.get(1).getId(),
                        library.get(2).getId(), library.get(3).getId());

        harness.handleCardChosen(player1, 0);

        var subsequentSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(subsequentSearch).isNotNull();
        assertThat(subsequentSearch.params().cards())
                .extracting(Card::getId)
                .containsExactly(library.get(1).getId(), library.get(2).getId(), library.get(3).getId());

        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }
        UUID sourcePermanentId = harness.getPermanentId(player1, "Parallel Thoughts");
        assertThat(gd.getCardsExiledByPermanent(sourcePermanentId)).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 4).stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(library.subList(4, 8).stream().map(Card::getId).toList());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May replace a draw with the top card of its exiled pile")
    void acceptsDrawReplacementFromPile() {
        ScornfulEgotist pileCard = new ScornfulEgotist();
        ScornfulEgotist libraryCard = new ScornfulEgotist();
        UUID sourcePermanentId = setupWithPile(List.of(pileCard));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        resolveDrawChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(pileCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId());
        assertThat(gd.getCardsExiledByPermanent(sourcePermanentId)).isEmpty();
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can replace each draw with the next card from its exiled pile")
    void replacesEachDrawWithNextPileCard() {
        ScornfulEgotist firstPileCard = new ScornfulEgotist();
        ScornfulEgotist secondPileCard = new ScornfulEgotist();
        ScornfulEgotist firstLibraryCard = new ScornfulEgotist();
        ScornfulEgotist secondLibraryCard = new ScornfulEgotist();
        UUID sourcePermanentId = setupWithPile(List.of(firstPileCard, secondPileCard));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstLibraryCard, secondLibraryCard));

        resolveDrawChoice();
        harness.handleMayAbilityChosen(player1, true);
        resolveDrawChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(firstPileCard.getId(), secondPileCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(firstLibraryCard.getId(), secondLibraryCard.getId());
        assertThat(gd.getCardsExiledByPermanent(sourcePermanentId)).isEmpty();
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Declining the replacement performs a normal draw")
    void declinesDrawReplacement() {
        ScornfulEgotist pileCard = new ScornfulEgotist();
        ScornfulEgotist libraryCard = new ScornfulEgotist();
        setupWithPile(List.of(pileCard));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        resolveDrawChoice();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(pileCard.getId());
    }

    @Test
    @DisplayName("Accepting with an empty pile still replaces the draw")
    void emptyPileStillReplacesDraw() {
        setupWithPile(List.of());
        harness.setHand(player1, List.of());
        ScornfulEgotist libraryCard = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(libraryCard));

        resolveDrawChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId());
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Replacing a draw from an empty library prevents the empty-library loss")
    void replacesDrawFromEmptyLibrary() {
        ScornfulEgotist pileCard = new ScornfulEgotist();
        UUID sourcePermanentId = setupWithPile(List.of(pileCard));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        resolveDrawChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(pileCard.getId());
        assertThat(gd.getCardsExiledByPermanent(sourcePermanentId)).isEmpty();
        assertThat(gd.winnerPlayerId).isNull();
    }

    private UUID setupWithPile(List<Card> pile) {
        harness.addToBattlefield(player1, new ParallelThoughts());
        UUID sourcePermanentId = harness.getPermanentId(player1, "Parallel Thoughts");
        for (Card card : pile) {
            gd.addToExile(player1.getId(), card, sourcePermanentId, true);
        }
        return sourcePermanentId;
    }

    private void resolveDrawChoice() {
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
