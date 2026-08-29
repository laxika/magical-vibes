package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FabledPassageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Fabled Passage and searches for a basic land onto the battlefield tapped")
    void activationSearchesForBasicLand() {
        addPassageWithLands(2);
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fabled Passage");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("The fetched land stays tapped when it is the third land")
    void fetchedLandStaysTappedBelowFourLands() {
        addPassageWithLands(2);
        setupLibrary();

        chooseFetchedLand();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Forest"))
                .singleElement()
                .matches(p -> p.isTapped());
    }

    @Test
    @DisplayName("The fetched land is untapped when it makes four lands")
    void fetchedLandUntapsAtFourLands() {
        addPassageWithLands(3);
        setupLibrary();

        chooseFetchedLand();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Forest"))
                .singleElement()
                .matches(p -> !p.isTapped());
    }

    private void chooseFetchedLand() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }

    private void addPassageWithLands(int landCount) {
        harness.addToBattlefield(player1, new FabledPassage());
        for (int i = 0; i < landCount; i++) {
            harness.addToBattlefield(player1, new Island());
        }
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Plains(), new GrizzlyBears()));
    }
}
