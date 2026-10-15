package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantFireBeetles;
import com.github.laxika.magicalvibes.cards.g.GateToManorborn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NavigationOrb.class, Forest.class, GateToManorborn.class, GiantFireBeetles.class})
class NavigationOrbTest extends BaseCardTest {

    @Test
    @DisplayName("It offers basic lands and Gates, then puts one tapped and one into hand")
    void searchesForBasicLandOrGate() {
        activateOrb(List.of(new Forest(), new GateToManorborn(), new GiantFireBeetles()));

        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch).isNotNull();
        assertThat(firstSearch.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(firstSearch.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Gate to Manorborn");

        int gateIndex = firstSearch.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Gate to Manorborn");
        harness.handleCardChosen(player1, gateIndex);

        PendingInteraction.LibrarySearch secondSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch).isNotNull();
        assertThat(secondSearch.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(secondSearch.params().cards()).extracting(Card::getName)
                .containsExactly("Forest");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GateToManorborn
                        && permanent.isTapped());
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Navigation Orb");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("It can fail to find when the library has no basic land or Gate")
    void canFailToFind() {
        activateOrb(List.of(new GiantFireBeetles()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Navigation Orb");
        harness.assertInGraveyard(player1, "Navigation Orb");
    }

    @Test
    @DisplayName("A single found basic land enters tapped and does not go to hand")
    void findsOnlyOneBasicLand() {
        activateOrb(List.of(new Forest(), new GiantFireBeetles()));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Giant Fire Beetles");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("It may find zero even when matching cards are available")
    void declinesEntireSearch() {
        activateOrb(List.of(new Forest(), new GateToManorborn()));

        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Gate to Manorborn");
        harness.assertNotInHand(player1, "Forest");
        harness.assertNotInHand(player1, "Gate to Manorborn");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("It may find one and decline the card for hand")
    void declinesSecondCard() {
        activateOrb(List.of(new Forest(), new GateToManorborn()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Gate to Manorborn");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Gate to Manorborn");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Both found cards may be Gates, including the card put into hand")
    void findsTwoGates() {
        activateOrb(List.of(new GateToManorborn(), new GateToManorborn()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GateToManorborn)
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof GateToManorborn).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Both found cards may be basic lands")
    void findsTwoBasicLands() {
        activateOrb(List.of(new Forest(), new Forest()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Forest)
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof Forest).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrifice is paid at activation before the search resolves")
    void sacrificesBeforeResolution() {
        harness.addToBattlefield(player1, new NavigationOrb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Navigation Orb");
        harness.assertInGraveyard(player1, "Navigation Orb");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Forest");

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("A tapped Orb cannot pay its tap cost")
    void cannotActivateWhenTapped() {
        harness.addToBattlefieldAndReturn(player1, new NavigationOrb()).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Navigation Orb");
        harness.assertNotInGraveyard(player1, "Navigation Orb");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana prevents activation and sacrifice")
    void cannotActivateWithoutTwoMana() {
        harness.addToBattlefield(player1, new NavigationOrb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Navigation Orb");
        harness.assertNotInGraveyard(player1, "Navigation Orb");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library ends the search without a choice")
    void searchesEmptyLibrary() {
        activateOrb(List.of());

        harness.assertInGraveyard(player1, "Navigation Orb");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both found cards are revealed and the remaining library is shuffled")
    void revealsCardsAndShuffles() {
        activateOrb(List.of(new Forest(), new GateToManorborn(), new GiantFireBeetles()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals Forest"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals Gate to Manorborn"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled"));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Giant Fire Beetles");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateOrb(List<Card> library) {
        harness.addToBattlefield(player1, new NavigationOrb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
