package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BiblioplexTomekeeper;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DinasGuidance.class, Plains.class, BiblioplexTomekeeper.class})
class DinasGuidanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only creature cards and reveals the choice")
    void resolvingOffersCreatureCards() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2).allMatch(card -> card.hasType(CardType.CREATURE));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND_OR_GRAVEYARD);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("The revealed creature can be put into hand")
    void putsRevealedCreatureIntoHand() {
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();

        Card chosen = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearchDestinationChoice.class);
        harness.handleListChoice(player1, "Hand");

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The revealed creature can be put into the graveyard")
    void putsRevealedCreatureIntoGraveyard() {
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();

        Card chosen = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, "Graveyard");

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chosen);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A restricted search can fail to find even with creatures available")
    void canFailToFindCreature() {
        setupAndCast();
        setupLibrary();
        List<Card> library = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof DinasGuidance);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library without creatures resolves without a destination choice")
    void resolvesWithoutMatchingCreature() {
        setupAndCast();
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof DinasGuidance);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library completes normally")
    void resolvesWithEmptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof DinasGuidance);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new DinasGuidance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new BiblioplexTomekeeper(), new BiblioplexTomekeeper()));
    }
}
