package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({BurningInquiry.class, RuneclawBear.class})
class BurningInquiryTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Burning Inquiry puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new BurningInquiry(), "{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(BurningInquiry.class);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new BurningInquiry()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving makes each player draw 3 then discard 3 at random")
    void resolvingDrawsAndDiscardsForEachPlayer() {
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new BurningInquiry(), "{R}");
        harness.passBothPriorities();

        // Player 1: cast spell (-1 card from hand), drew 3, discarded 3 at random = 0 cards in hand
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Player 1 deck lost 3 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 3);
        // Player 1 graveyard: Burning Inquiry + 3 discarded = 4
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);

        // Player 2: started with empty hand, drew 3, discarded 3 at random = 0 cards in hand
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        // Player 2 deck lost 3 cards
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 3);
        // Player 2 graveyard: 3 discarded
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);

        // Should NOT be awaiting any input (random discard doesn't prompt)
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Log shows random discard for each player")
    void logShowsRandomDiscardForEachPlayer() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new BurningInquiry(), "{R}");
        harness.passBothPriorities();

        long randomDiscardLogs = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        // 3 discards for player1 + 3 discards for player2 = 6
        assertThat(randomDiscardLogs).isEqualTo(6);
    }

    @Test
    @DisplayName("Burning Inquiry goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new BurningInquiry(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Burning Inquiry");
    }

    @Test
    @DisplayName("When a player has fewer cards than 3 after drawing, discards all available")
    void discardsAllWhenFewerThanThreeCardsAfterDraw() {
        // Give player2 a very small deck so they draw fewer than 3
        harness.setLibrary(player2, List.of(new RuneclawBear()));

        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new BurningInquiry(), "{R}");
        harness.passBothPriorities();

        // Player 2 drew only 1 card (deck ran out), discards 3 at random but only 1 available
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        // Player 2 graveyard has 1 discarded card
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each player retains their previous hand size after drawing and discarding")
    void discardsExactlyThreeFromNonemptyHands() {
        harness.setHand(player1, List.of(new BurningInquiry(), new RuneclawBear(), new RuneclawBear()));
        harness.setHand(player2, List.of(new RuneclawBear()));
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear(), new RuneclawBear()));
        harness.setLibrary(player2, List.of(new RuneclawBear(), new RuneclawBear(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The active player performs all three draws before the opponent")
    void activePlayerDrawsFirstWhenSecondInSeatingOrder() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear(), new RuneclawBear()));
        harness.setLibrary(player2, List.of(new RuneclawBear(), new RuneclawBear(), new RuneclawBear()));
        harness.castFromHand(player2, new BurningInquiry(), "{R}");
        gd.gameLog.clear();

        harness.passBothPriorities();

        String activeDraw = gd.playerIdToName.get(player2.getId()) + " draws a card.";
        String opponentDraw = gd.playerIdToName.get(player1.getId()) + " draws a card.";
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.endsWith(" draws a card.")).toList())
                .containsExactly(activeDraw, activeDraw, activeDraw, opponentDraw, opponentDraw, opponentDraw);
    }
}
