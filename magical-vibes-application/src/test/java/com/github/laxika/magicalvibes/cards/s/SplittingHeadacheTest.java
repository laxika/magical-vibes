package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SplittingHeadacheTest extends BaseCardTest {

    private void castMode(int modeIndex, java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SplittingHeadache()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, modeIndex, targetPlayerId);
        harness.passBothPriorities();
    }

    @Nested
    @DisplayName("Mode 0: Target player discards two cards")
    class DiscardTwoMode {

        @Test
        @DisplayName("Target player discards two chosen cards")
        void targetDiscardsTwo() {
            harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new GrizzlyBears(), new Peek())));

            castMode(0, player2.getId());

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player2, 0);
            harness.handleCardChosen(player2, 0);

            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        }

        @Test
        @DisplayName("Target player with only one card discards just that card")
        void targetDiscardsWholeSmallHand() {
            harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

            castMode(0, player2.getId());

            // Fewer cards than the discard count: the whole hand goes.
            if (gd.interaction.activeInteraction() != null) {
                harness.handleCardChosen(player2, 0);
            }
            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Mode 1: Reveal hand, you choose a card, that player discards it")
    class RevealChooseMode {

        @Test
        @DisplayName("Controller chooses a card from the revealed hand to discard")
        void controllerChoosesDiscard() {
            Card keep = new GrizzlyBears();
            Card toDiscard = new Peek();
            harness.setHand(player2, new ArrayList<>(List.of(toDiscard, keep)));

            castMode(1, player2.getId());

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                    .isEqualTo(player1.getId());

            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Peek"));
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
        }

        @Test
        @DisplayName("Empty hand does nothing")
        void emptyHandDoesNothing() {
            harness.setHand(player2, List.of());

            castMode(1, player2.getId());

            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }
}
