package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeaconOfUnrestTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Beacon of Unrest puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Beacon of Unrest");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Returning creature from own graveyard =====

    @Test
    @DisplayName("Returns creature from own graveyard to battlefield")
    void returnsCreatureFromOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Returning artifact from own graveyard =====

    @Test
    @DisplayName("Returns artifact from own graveyard to battlefield")
    void returnsArtifactFromOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new AngelsFeather()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel's Feather"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Angel's Feather"));
    }

    // ===== Returning from opponent's graveyard =====

    @Test
    @DisplayName("Returns creature from opponent's graveyard under your control")
    void returnsCreatureFromOpponentGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        // The only card in the pool is the opponent's Grizzly Bears at index 0
        harness.handleGraveyardCardChosen(player1, 0);

        // Card goes onto player1's battlefield (under your control)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Removed from opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Shuffle into library =====

    @Test
    @DisplayName("Beacon is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        // Not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Beacon of Unrest"));
        // In library (deck size increased by 1)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Card exists somewhere in the deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beacon of Unrest"));
        // Log confirms shuffle
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffled into its owner's library"));
    }

    @Test
    @DisplayName("Resolution state is fully consumed — exactly one Beacon in the deck, no dangling resumption")
    void noDanglingResumptionAfterChoice() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        // A dangling entry would re-run ShuffleIntoLibraryEffect from a later unrelated
        // interaction, putting a second Beacon copy into the deck
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Beacon of Unrest"))
                .hasSize(1);
    }

    // ===== Empty graveyards =====

    @Test
    @DisplayName("No valid targets when graveyards have no artifact or creature cards — spell fizzles, goes to graveyard")
    void noValidTargetsInGraveyards() {
        harness.setGraveyard(player1, List.of(new HolyDay()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Should not be awaiting graveyard choice since no valid targets
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no artifact or creature cards in any graveyard"));
        // Per Magic rules: spell fizzles when no legal targets — Beacon goes to graveyard, NOT shuffled into library
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beacon of Unrest"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("shuffled into its owner's library"));
    }

    // ===== Cards from both graveyards =====

    @Test
    @DisplayName("Can choose from cards across both graveyards")
    void choosesFromBothGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new AngelsFeather()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        // Pool should have 2 cards (Grizzly Bears from player1 + Angel's Feather from player2)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).cardPool()).hasSize(2);

        // Choose the artifact from opponent's graveyard (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel's Feather"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Angel's Feather"));
        // Grizzly Bears stays in player1's graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Stack is empty after resolution =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(harness.getGameData().stack).isEmpty();
    }
}

