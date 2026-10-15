package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.b.BosiumStrip;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncreasingAmbition.class, Plains.class, Swamp.class, BlackCat.class, BosiumStrip.class})
class IncreasingAmbitionTest extends BaseCardTest {


    @Test
    @DisplayName("Cast from hand searches the library for one card")
    void normalCastSearchesForOneCard() {
        harness.setHand(player1, List.of(new IncreasingAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 0);
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery -> library search prompt

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        // Exactly one card searched into hand, no further search pending
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }


    @Test
    @DisplayName("Cast from graveyard via flashback searches the library for two cards")
    void flashbackCastSearchesForTwoCards() {
        harness.setGraveyard(player1, List.of(new IncreasingAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 8); // pays {7}{B}
        harness.castFlashback(player1, 0);
        setupLibrary();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve flashback sorcery -> library search prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().remainingCount()).isEqualTo(2);

        // First pick
        harness.handleCardChosen(player1, 0);

        // A second pick is prompted
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().remainingCount()).isEqualTo(1);

        // Second pick
        harness.handleCardChosen(player1, 0);

        // Two cards searched into hand, search complete
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback exiles Increasing Ambition after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new IncreasingAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castFlashback(player1, 0);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Increasing Ambition");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Ambition"));
    }


    @Test
    @DisplayName("Resolving with an empty library logs and does not crash")
    void emptyLibrary() {
        harness.setHand(player1, List.of(new IncreasingAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 0);
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("it is empty"));
    }

    @Test
    @DisplayName("Flashback finds the only card in a one-card library")
    void flashbackWithOneCardInLibrary() {
        IncreasingAmbition ambition = new IncreasingAmbition();
        BlackCat found = new BlackCat();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(ambition));
        harness.setLibrary(player1, List.of(found));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(found);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ambition);
    }

    @Test
    @DisplayName("Flashback can find two cards with the same name")
    void flashbackCanFindSameNameTwice() {
        BlackCat first = new BlackCat();
        BlackCat second = new BlackCat();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new IncreasingAmbition()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bösium Strip permits the normal mana cost and searches for two cards")
    void graveyardCastUsingBosiumStripSearchesForTwoCards() {
        IncreasingAmbition ambition = new IncreasingAmbition();
        BlackCat first = new BlackCat();
        BlackCat second = new BlackCat();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(ambition));
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new BosiumStrip());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ambition);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new BlackCat(), new BlackCat()));
    }
}
