package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeaconOfImmortalityTest {

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
    @DisplayName("Beacon of Immortality has correct card properties")
    void hasCorrectProperties() {
        BeaconOfImmortality card = new BeaconOfImmortality();

        assertThat(card.getName()).isEqualTo("Beacon of Immortality");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{5}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DoubleTargetPlayerLifeEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ShuffleIntoLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Beacon of Immortality puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, "W", 6);

        harness.castInstant(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Beacon of Immortality");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, "W", 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Doubling life =====

    @Test
    @DisplayName("Resolving doubles target player's life total from 20 to 40")
    void doublesLifeFrom20To40() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, "W", 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(40);
        assertThat(harness.getGameData().gameLog).anyMatch(log -> log.contains("doubled from 20 to 40"));
    }

    @Test
    @DisplayName("Can target opponent to double their life")
    void canTargetOpponent() {
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, "W", 6);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Doubles low life total correctly")
    void doublesLowLifeTotal() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, "W", 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(6);
    }

    // ===== Shuffle into library =====

    @Test
    @DisplayName("Beacon is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, "W", 6);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Beacon of Immortality"));
        // In library (deck size increased by 1)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Card exists somewhere in the deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beacon of Immortality"));
        // Log confirms shuffle
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffled into its owner's library"));
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, "W", 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }
}
