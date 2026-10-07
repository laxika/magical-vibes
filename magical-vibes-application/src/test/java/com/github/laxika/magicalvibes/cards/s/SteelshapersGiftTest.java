package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChimericCoils;
import com.github.laxika.magicalvibes.cards.g.GraftedWargear;
import com.github.laxika.magicalvibes.cards.v.Vanquish;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteelshapersGift.class, GraftedWargear.class, ChimericCoils.class, Vanquish.class})
class SteelshapersGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only Equipment cards for the search")
    void resolvingPresentsOnlyEquipment() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.EQUIPMENT));
    }

    @Test
    @DisplayName("Chosen Equipment goes to hand and the library is shuffled")
    void chosenEquipmentGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getSubtypes().contains(CardSubtype.EQUIPMENT));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May fail to find an Equipment even when one is in the library")
    void mayFailToFindEquipment() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c ->
                c.getSubtypes().contains(CardSubtype.EQUIPMENT));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getSubtypes().contains(CardSubtype.EQUIPMENT));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No prompt is created when the library holds no Equipment")
    void noEquipmentInLibrary() {
        setupAndCast();
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new ChimericCoils(), new Vanquish()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no"));
    }

    @Test
    @DisplayName("An empty library still completes the search and shuffle")
    void emptyLibraryCompletesResolution() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Steelshaper's Gift");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled."));
    }

    @Test
    @DisplayName("Only the selected Equipment is revealed and moved to the controller's hand")
    void choosesExactlyOneEquipmentFromMultipleMatches() {
        setupAndCast();
        GraftedWargear firstEquipment = new GraftedWargear();
        GraftedWargear chosenEquipment = new GraftedWargear();
        ChimericCoils nonEquipment = new ChimericCoils();
        GraftedWargear opponentsEquipment = new GraftedWargear();
        harness.setLibrary(player1, List.of(nonEquipment, firstEquipment, chosenEquipment));
        harness.setLibrary(player2, List.of(opponentsEquipment));
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenEquipment);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonEquipment, firstEquipment);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsEquipment);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Steelshaper's Gift");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals Grafted Wargear")
                        && entry.contains("puts it into their hand"));
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new SteelshapersGift(), "{W}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new ChimericCoils(), new GraftedWargear(), new Vanquish()));
    }
}
