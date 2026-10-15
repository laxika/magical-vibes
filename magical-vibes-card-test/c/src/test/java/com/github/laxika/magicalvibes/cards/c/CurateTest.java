package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Curate.class, ArgothianSprite.class})
class CurateTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 2 before drawing a card")
    void surveilsThenDraws() {
        Card milledCard = new ArgothianSprite();
        Card drawnCard = new ArgothianSprite();
        harness.setLibrary(player1, List.of(milledCard, drawnCard));
        harness.castFromHand(player1, new Curate(), "{1}{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void keepingBothCardsAllowsReorderingBeforeDrawing() {
        Card firstCard = new ArgothianSprite();
        Card secondCard = new ArgothianSprite();
        Card thirdCard = new ArgothianSprite();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.castFromHand(player1, new Curate(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstCard, thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(firstCard, secondCard, thirdCard);
        harness.assertInGraveyard(player1, "Curate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void puttingBothCardsIntoGraveyardDrawsThirdCard() {
        Card firstCard = new ArgothianSprite();
        Card secondCard = new ArgothianSprite();
        Card thirdCard = new ArgothianSprite();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.castFromHand(player1, new Curate(), "{1}{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard, secondCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Curate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void keepingOnlyLibraryCardDrawsItWithoutLosing() {
        Card onlyCard = new ArgothianSprite();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new Curate(), "{1}{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Curate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void puttingOnlyLibraryCardIntoGraveyardLosesOnDraw() {
        Card onlyCard = new ArgothianSprite();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new Curate(), "{1}{U}");
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void emptyLibraryStillAttemptsDrawAndLosesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Curate(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
