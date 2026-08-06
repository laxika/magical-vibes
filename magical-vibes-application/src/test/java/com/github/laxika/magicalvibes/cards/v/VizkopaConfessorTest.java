package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VizkopaConfessorTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    private void castConfessor(UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new VizkopaConfessor())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger -> prompts for the life payment
    }

    @Test
    @DisplayName("Paying 2 life makes the opponent reveal two cards; the controller exiles one of them")
    void payTwoLifeRevealTwoExileOne() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears())));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castConfessor(player2.getId());

        harness.handleXValueChosen(player1, 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(reveal.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1); // reveal Hill Giant as the second card

        PendingInteraction.RevealCardsDiscardChoice pick = activeChoice();
        assertThat(pick.revealStage()).isFalse();
        assertThat(pick.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pick.revealedCardIds()).hasSize(2);

        harness.handleCardChosen(player1, 1); // exile Hill Giant

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.exiledCards.stream().map(e -> e.card().getName())).contains("Hill Giant");
    }

    @Test
    @DisplayName("Paying 0 life reveals nothing and exiles nothing")
    void payZeroLifeDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castConfessor(player2.getId());

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A hand no larger than the life paid is revealed whole, skipping the opponent's reveal choice")
    void wholeHandRevealedWhenNotLarger() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castConfessor(player2.getId());
        harness.handleXValueChosen(player1, 3);

        PendingInteraction.RevealCardsDiscardChoice pick = activeChoice();
        assertThat(pick.revealStage()).isFalse();
        assertThat(pick.decidingPlayerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getName())).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("The opponent cannot make the controller's exile pick")
    void wrongPlayerCannotMakeExilePick() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castConfessor(player2.getId());
        harness.handleXValueChosen(player1, 1);

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }
}
