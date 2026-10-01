package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.cards.w.WaspLancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplittingHeadache.class, Cinderbones.class, WaspLancer.class})
class SplittingHeadacheTest extends BaseCardTest {

    private void castMode(int modeIndex, java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SplittingHeadache()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castModalSorcery(player1, 0, modeIndex, List.of(targetPlayerId));
        harness.passBothPriorities();
    }

    @Nested
    @DisplayName("Mode 0: Target player discards two cards")
    class DiscardTwoMode {

        @Test
        @DisplayName("Target player discards two chosen cards")
        void targetDiscardsTwo() {
            harness.setHand(player2, List.of(new Cinderbones(), new WaspLancer(), new Cinderbones()));

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
            harness.setHand(player2, List.of(new Cinderbones()));

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
            Card keep = new WaspLancer();
            Card toDiscard = new Cinderbones();
            harness.setHand(player2, List.of(toDiscard, keep));

            castMode(1, player2.getId());

            assertThat(gameLogContains(player2.getUsername() + " reveals their hand")).isTrue();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                    .isEqualTo(player1.getId());

            harness.handleCardChosen(player1, 0);

            harness.assertInGraveyard(player2, "Cinderbones");
            assertThat(gd.playerHands.get(player2.getId())).containsExactly(keep);
        }

        @Test
        @DisplayName("Empty hand does nothing")
        void emptyHandDoesNothing() {
            harness.setHand(player2, List.of());

            castMode(1, player2.getId());

            assertThat(gameLogContains(player2.getUsername() + " reveals their hand")).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Both modes require a player target")
    void bothModesRequirePlayerTarget() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new WaspLancer());
        harness.setHand(player1, List.of(new SplittingHeadache()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target player may be the caster")
    void targetMayBeCaster() {
        Card first = new Cinderbones();
        Card second = new WaspLancer();
        harness.setHand(player1, List.of(new SplittingHeadache(), first, second));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castModalSorcery(player1, 0, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }
}
