package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeaconOfDestructionTest {

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
    @DisplayName("Beacon of Destruction has correct card properties")
    void hasCorrectProperties() {
        BeaconOfDestruction card = new BeaconOfDestruction();

        assertThat(card.getName()).isEqualTo("Beacon of Destruction");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{3}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(((DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.SPELL).get(0)).damage()).isEqualTo(5);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ShuffleIntoLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Beacon of Destruction targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Beacon of Destruction");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Beacon of Destruction targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Beacon of Destruction");
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Dealing damage to player =====

    @Test
    @DisplayName("Deals 5 damage to target player")
    void deals5DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can target yourself to deal 5 damage")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    // ===== Dealing damage to creature =====

    @Test
    @DisplayName("Deals 5 damage to target creature, destroying it")
    void deals5DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Grizzly Bears (2/2) should be destroyed by 5 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Shuffle into library =====

    @Test
    @DisplayName("Beacon is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Beacon of Destruction"));
        // In library (deck size increased by 1)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Card exists somewhere in the deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beacon of Destruction"));
        // Log confirms shuffle
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffled into its owner's library"));
    }

    @Test
    @DisplayName("Beacon is shuffled into library even when targeting a creature")
    void shuffledIntoLibraryWhenTargetingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Beacon of Destruction"));
        // In library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beacon of Destruction"));
    }

    // ===== Stack cleanup =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }
}
