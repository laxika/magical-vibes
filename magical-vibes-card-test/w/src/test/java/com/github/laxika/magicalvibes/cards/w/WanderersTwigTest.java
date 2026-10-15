package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderersTwig.class, Plains.class, Forest.class, Island.class, GrizzlyBears.class})
class WanderersTwigTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices the twig and searches library for basic land")
    void activateAbilitySacrificesAndSearches() {
        harness.addToBattlefield(player1, new WanderersTwig());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibraryWithBasicLands();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Twig should be sacrificed
        harness.assertNotOnBattlefield(player1, "Wanderer's Twig");
        harness.assertInGraveyard(player1, "Wanderer's Twig");

        // Should be awaiting library search with only basic lands offered
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Choosing a basic land from search puts it into hand")
    void choosingBasicLandPutsItIntoHand() {
        harness.addToBattlefield(player1, new WanderersTwig());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibraryWithBasicLands();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    @Test
    @DisplayName("Searching an empty library completes without a choice")
    void emptyLibraryAutoCompletes() {
        harness.addToBattlefield(player1, new WanderersTwig());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        // Twig should be sacrificed
        harness.assertNotOnBattlefield(player1, "Wanderer's Twig");
        harness.assertInGraveyard(player1, "Wanderer's Twig");
    }

    private void setupLibraryWithBasicLands() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }

    @Test
    @DisplayName("A restricted search may fail to find even with basic lands available")
    void mayFailToFind() {
        harness.addToBattlefield(player1, new WanderersTwig());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibraryWithBasicLands();
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(originalLibrary);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wanderer's Twig");
    }

    @Test
    @DisplayName("A library without basic lands completes the search without taking a card")
    void noBasicLandsCompletesSearch() {
        harness.addToBattlefield(player1, new WanderersTwig());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Card nonland = new WanderersTwig();
        harness.setLibrary(player1, List.of(nonland));
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Twig is sacrificed as a cost and its ability reveals a land on resolution")
    void sacrificeIsPaidBeforeSearchResolves() {
        harness.addToBattlefield(player1, new WanderersTwig());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Card land = new Forest();
        Card remainingCard = new WanderersTwig();
        harness.setLibrary(player1, List.of(land, remainingCard));
        List<Card> opponentHand = List.copyOf(gd.playerHands.get(player2.getId()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Wanderer's Twig");
        harness.assertNotOnBattlefield(player1, "Wanderer's Twig");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, remainingCard);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHand);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals Forest")
                && entry.plainText().contains("Library is shuffled"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
