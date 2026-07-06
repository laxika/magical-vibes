package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEqualToTargetPlayerHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorrowedKnowledgeTest extends BaseCardTest {

    

    @Nested
    @DisplayName("Mode 0: draw equal to target opponent's hand size")
    class OpponentHandSizeMode {

        @Test
        @DisplayName("Discards remaining hand then draws equal to opponent's hand size")
        void discardsHandThenDrawsEqualToOpponentHandSize() {
            setDeck(player1, List.of(new Island(), new Island(), new Island(), new Island()));
            harness.setHand(player1, List.of(
                    new BorrowedKnowledge(),
                    new GrizzlyBears(),
                    new GrizzlyBears()));
            harness.setHand(player2, List.of(new Plains(), new Island(), new GrizzlyBears()));
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
            assertThat(gd.playerHands.get(player1.getId()))
                    .allMatch(c -> c.getName().equals("Island"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Borrowed Knowledge"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Uses opponent's hand size at resolution time")
        void usesOpponentHandSizeOnResolution() {
            setDeck(player1, List.of(new Island(), new Island(), new Island(), new Island()));
            harness.setHand(player1, List.of(new BorrowedKnowledge(), new GrizzlyBears()));
            harness.setHand(player2, List.of(new Plains(), new Island()));
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 0, player2.getId());
            gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        }

        @Test
        @DisplayName("With empty hand after casting, still draws equal to opponent's hand size")
        void emptyHandStillDrawsFromOpponentHandSize() {
            setDeck(player1, List.of(new Island(), new Island(), new Island()));
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
            setDeck(player1, List.of(new Island()));
            harness.setHand(player1, List.of(new BorrowedKnowledge(), new GrizzlyBears()));
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
    @DisplayName("Mode 1: draw equal to cards discarded")
    class DiscardedCountMode {

        @Test
        @DisplayName("Discards remaining hand then draws that many cards")
        void discardsHandThenDrawsThatMany() {
            setDeck(player1, List.of(new Island(), new Island(), new Island()));
            harness.setHand(player1, List.of(
                    new BorrowedKnowledge(),
                    new GrizzlyBears(),
                    new GrizzlyBears(),
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
            setDeck(player1, List.of(new Island()));
            harness.setHand(player1, List.of(new BorrowedKnowledge()));
            addManaForCast(player1);

            harness.castSorcery(player1, 0, 1);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
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

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
