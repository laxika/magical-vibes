package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinLore.class, GrizzlyBears.class})
class GoblinLoreTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Goblin Lore puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new GoblinLore(), "{1}{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
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

        harness.castFromHand(player1, new GoblinLore(), "{1}{R}");
        harness.passBothPriorities();

        // Drew 4 cards, discarded 3 at random — net gain of 1 card
        // Spell left hand (-1), drew 4, discarded 3 at random = 0 cards from original hand + 1 net = 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck should have lost 4 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 4);
        // 3 cards should have been discarded at random (graveyard has Goblin Lore + 3 discarded)
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        // Should NOT be awaiting any input (random discard doesn't prompt)
        assertThat(gd.interaction.activeInteraction()).isNull();
        // Log should mention discards at random
        long randomDiscardLogs = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(3);
    }

    @Test
    @DisplayName("Goblin Lore goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new GoblinLore(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Lore");
    }

    @Test
    @DisplayName("When hand has fewer than 3 cards after drawing, discards all available")
    void discardsAllWhenFewerThanThreeCardsAfterDraw() {
        // Drawing beyond the two-card library also causes the expected game loss.
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castFromHand(player1, new GoblinLore(), "{1}{R}");
        harness.passBothPriorities();

        // Drew 2 (deck ran out), discard 3 at random but only 2 available — discards all 2
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Graveyard has Goblin Lore + 2 discarded
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}

