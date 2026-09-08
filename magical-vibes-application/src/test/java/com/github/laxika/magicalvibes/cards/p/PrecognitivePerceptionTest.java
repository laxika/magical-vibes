package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrecognitivePerceptionTest extends BaseCardTest {

    @Test
    void addendumScriesThreeThenDrawsThreeDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top = deck.get(0);
        Card second = deck.get(1);
        Card third = deck.get(2);

        harness.setHand(player1, List.of(new PrecognitivePerception()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(3);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top, second, third);
    }

    @Test
    void drawsThreeWithoutAddendumOutsideMainPhase() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top = deck.get(0);
        Card second = deck.get(1);
        Card third = deck.get(2);

        harness.setHand(player1, List.of(new PrecognitivePerception()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top, second, third);
    }
}
