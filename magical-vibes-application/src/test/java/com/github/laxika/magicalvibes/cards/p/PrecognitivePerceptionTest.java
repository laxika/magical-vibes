package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.IncreasingVengeance;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrecognitivePerception.class, IncreasingVengeance.class})
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
        harness.castAndResolveInstant(player1, 0);

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
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top, second, third);
    }

    @Test
    void addendumAppliesDuringPostcombatMainPhase() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        List<Card> expected = List.copyOf(gd.playerDecks.get(player1.getId()).subList(0, 3));
        harness.setHand(player1, List.of(new PrecognitivePerception()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(3);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(expected);
    }

    @Test
    void drawsThreeWithoutScryDuringOpponentsMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        List<Card> expected = List.copyOf(gd.playerDecks.get(player1.getId()).subList(0, 3));
        harness.setHand(player1, List.of(new PrecognitivePerception()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(expected);
    }

    @Test
    void drawsFromLibraryAfterBottomingAllThreeScryedCards() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        List<Card> bottomed = List.copyOf(deck.subList(0, 3));
        List<Card> expected = List.copyOf(deck.subList(3, 6));
        harness.setHand(player1, List.of(new PrecognitivePerception()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(expected);
        assertThat(deck.subList(deck.size() - 3, deck.size()))
                .containsExactly(bottomed.get(2), bottomed.get(0), bottomed.get(1));
    }

    @Test
    void copyDoesNotGetAddendumEvenWhenOriginalWasCastDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        PrecognitivePerception original = new PrecognitivePerception();
        List<Card> expected = List.copyOf(gd.playerDecks.get(player1.getId()).subList(0, 3));
        harness.setHand(player1, List.of(original, new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(expected);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(3);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }
}
