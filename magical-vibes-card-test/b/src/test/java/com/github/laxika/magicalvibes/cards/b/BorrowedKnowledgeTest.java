package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorrowedKnowledge.class, Island.class, Plains.class})
class BorrowedKnowledgeTest extends BaseCardTest {

    

    @Nested
    @CardUsed({BorrowedKnowledge.class, Island.class, Plains.class})
    @DisplayName("Mode 0: draw equal to target opponent's hand size")
    class OpponentHandSizeMode {

        @Test
        @DisplayName("Discards remaining hand then draws equal to opponent's hand size")
        void discardsHandThenDrawsEqualToOpponentHandSize() {
            harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
            harness.setHand(player1, List.of(
                    new BorrowedKnowledge(),
                    new Plains(),
                    new Plains()));
            harness.setHand(player2, List.of(new Plains(), new Island(), new Plains()));
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
            assertThat(gd.playerHands.get(player1.getId()))
                    .allMatch(c -> c.getName().equals("Island"));
            harness.assertInGraveyard(player1, "Borrowed Knowledge");
            harness.assertInGraveyard(player1, "Plains");
        }

        @Test
        @DisplayName("Uses opponent's hand size at resolution time")
        void usesOpponentHandSizeOnResolution() {
            harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
            harness.setHand(player1, List.of(new BorrowedKnowledge(), new Plains()));
            harness.setHand(player2, List.of(new Plains(), new Island()));
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 0, player2.getId());
            gd.playerHands.get(player2.getId()).add(new Plains());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        }

        @Test
        @DisplayName("With empty hand after casting, still draws equal to opponent's hand size")
        void emptyHandStillDrawsFromOpponentHandSize() {
            harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
            harness.setHand(player1, List.of(new BorrowedKnowledge()));
            harness.setHand(player2, List.of(new Plains(), new Island()));
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
            assertThat(gd.playerHands.get(player1.getId()))
                    .allMatch(c -> c.getName().equals("Island"));
        }

        @Test
        @DisplayName("Draws nothing when opponent has empty hand")
        void drawsNothingWhenOpponentHandEmpty() {
            harness.setLibrary(player1, List.of(new Island()));
            harness.setHand(player1, List.of(new BorrowedKnowledge(), new Plains()));
            harness.setHand(player2, List.of());
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Cannot target self")
        void cannotTargetSelf() {
            harness.setHand(player1, List.of(new BorrowedKnowledge()));
            addManaForCast(player1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player1.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @CardUsed({BorrowedKnowledge.class, Island.class, Plains.class})
    @DisplayName("Mode 1: draw equal to cards discarded")
    class DiscardedCountMode {

        @Test
        @DisplayName("Discards remaining hand then draws that many cards")
        void discardsHandThenDrawsThatMany() {
            harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
            harness.setHand(player1, List.of(
                    new BorrowedKnowledge(),
                    new Plains(),
                    new Plains(),
                    new Island()));
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 1);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
            assertThat(gd.playerHands.get(player1.getId()))
                    .allMatch(c -> c.getName().equals("Island"));
        }

        @Test
        @DisplayName("With empty hand after casting, discards nothing and draws nothing")
        void emptyHandDoesNothing() {
            harness.setLibrary(player1, List.of(new Island()));
            harness.setHand(player1, List.of(new BorrowedKnowledge()));
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 1);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Counts the cards actually discarded at resolution, leaving the opponent's hand intact")
        void countsHandAtResolutionAndLeavesOpponentHandIntact() {
            Island firstDraw = new Island();
            Island secondDraw = new Island();
            Plains firstDiscard = new Plains();
            Plains secondDiscard = new Plains();
            Island opponentCard = new Island();
            harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Island()));
            harness.setHand(player1, List.of(new BorrowedKnowledge(), firstDiscard));
            harness.setHand(player2, List.of(opponentCard));
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 1);
            harness.setHand(player1, List.of(firstDiscard, secondDiscard));
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .contains(firstDiscard, secondDiscard);
            assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        }
    }

    @Test
    @DisplayName("Modal cast writes mode targeting onto a runtime copy, never the shared original card")
    void modalCastDoesNotMutateOriginalCard() {
        harness.setLibrary(player1, List.of(new Island()));
        BorrowedKnowledge original = new BorrowedKnowledge();
        // Deck cards are frozen at game setup; setHand bypasses setup, so freeze explicitly.
        // If the modal cast mutated the original instead of a runtime copy, casting would throw.
        original.freeze();
        harness.setHand(player1, List.of(original));
        addManaForCast(player1);

        harness.castSorcery(player1, 0, 0, player2.getId());

        Card onStack = gd.stack.getLast().getCard();
        assertThat(onStack).isNotSameAs(original);
        assertThat(onStack.getId()).isEqualTo(original.getId());
        assertThat(original.getCastTimeTargetFilter()).isNull();

        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(original.getId()));
    }

    @Test
    @DisplayName("Choosing invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new BorrowedKnowledge()));
        addManaForCast(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    private void addManaForCast(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

}
