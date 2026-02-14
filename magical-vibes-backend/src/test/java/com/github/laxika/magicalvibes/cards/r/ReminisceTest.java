package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReminisceTest {

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
    @DisplayName("Reminisce has correct card properties")
    void hasCorrectProperties() {
        Reminisce card = new Reminisce();

        assertThat(card.getName()).isEqualTo("Reminisce");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ShuffleGraveyardIntoLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Reminisce puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Reminisce");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving — target self =====

    @Test
    @DisplayName("Shuffles own graveyard into library")
    void shufflesOwnGraveyardIntoLibrary() {
        Card bear1 = new GrizzlyBears();
        Card bear2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear1, bear2));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Graveyard should be empty (Reminisce itself goes to graveyard after resolution)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // Deck size should increase by 2 (the two bears from graveyard)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 2);
        // Bears should be in library
        assertThat(gd.playerDecks.get(player1.getId()).stream().filter(c -> c.getName().equals("Grizzly Bears")).count()).isEqualTo(2);
        // Log confirms shuffle
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their graveyard"));
    }

    // ===== Resolving — target opponent =====

    @Test
    @DisplayName("Can target opponent to shuffle their graveyard into their library")
    void canTargetOpponent() {
        Card bear = new GrizzlyBears();
        Card giant = new GiantSpider();
        harness.setGraveyard(player2, List.of(bear, giant));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Opponent's graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears") || c.getName().equals("Giant Spider"));
        // Opponent's deck should grow by 2
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 2);
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Resolving with empty graveyard still shuffles library")
    void emptyGraveyardStillShufflesLibrary() {
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Deck size unchanged
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        // Log indicates empty graveyard
        assertThat(gd.gameLog).anyMatch(log -> log.contains("graveyard is empty"));
    }

    @Test
    @DisplayName("Reminisce itself goes to graveyard after resolution")
    void reminisceGoesToGraveyardAfterResolution() {
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Reminisce should be in graveyard (it resolves first, shuffling the empty graveyard, then goes to graveyard itself)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reminisce"));
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }
}
