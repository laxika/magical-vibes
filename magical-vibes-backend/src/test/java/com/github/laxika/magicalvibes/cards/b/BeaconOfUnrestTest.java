package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeaconOfUnrestTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Beacon of Unrest has correct card properties")
    void hasCorrectProperties() {
        BeaconOfUnrest card = new BeaconOfUnrest();

        assertThat(card.getName()).isEqualTo("Beacon of Unrest");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{3}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ShuffleIntoLibraryEffect.class);
    }

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
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

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
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

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
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

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
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffled into its owner's library"));
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
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no artifact or creature cards in any graveyard"));
        // Per Magic rules: spell fizzles when no legal targets — Beacon goes to graveyard, NOT shuffled into library
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beacon of Unrest"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog).noneMatch(log -> log.contains("shuffled into its owner's library"));
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
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        // Pool should have 2 cards (Grizzly Bears from player1 + Angel's Feather from player2)
        assertThat(gd.interaction.graveyardChoiceCardPool()).hasSize(2);

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


