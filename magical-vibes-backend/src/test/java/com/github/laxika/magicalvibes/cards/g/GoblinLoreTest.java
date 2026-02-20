package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RandomDiscardEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinLoreTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Goblin Lore has correct card properties")
    void hasCorrectProperties() {
        GoblinLore card = new GoblinLore();

        assertThat(card.getName()).isEqualTo("Goblin Lore");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{1}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect drawEffect = (DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(drawEffect.amount()).isEqualTo(4);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(RandomDiscardEffect.class);
        RandomDiscardEffect discardEffect = (RandomDiscardEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(discardEffect.amount()).isEqualTo(3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Goblin Lore puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GoblinLore()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Goblin Lore");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new GoblinLore()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving draws four cards then discards three at random")
    void resolvingDrawsFourThenDiscardsThreeAtRandom() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new GoblinLore()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Drew 4 cards, discarded 3 at random — net gain of 1 card
        // Spell left hand (-1), drew 4, discarded 3 at random = 0 cards from original hand + 1 net = 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck should have lost 4 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 4);
        // 3 cards should have been discarded at random (graveyard has Goblin Lore + 3 discarded)
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        // Should NOT be awaiting any input (random discard doesn't prompt)
        assertThat(gd.interaction.awaitingInputType()).isNull();
        // Log should mention discards at random
        long randomDiscardLogs = gd.gameLog.stream()
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(3);
    }

    @Test
    @DisplayName("Goblin Lore goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new GoblinLore()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Lore"));
    }

    @Test
    @DisplayName("When hand has fewer than 3 cards after drawing, discards all available")
    void discardsAllWhenFewerThanThreeCardsAfterDraw() {
        // Set up: empty deck so drawing 4 won't yield 4 cards
        gd.playerDecks.get(player1.getId()).clear();
        // Add only 2 cards to deck
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new GoblinLore()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Drew 2 (deck ran out), discard 3 at random but only 2 available — discards all 2
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Graveyard has Goblin Lore + 2 discarded
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}


