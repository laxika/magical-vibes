package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Diresight.class, BarkformHarvester.class, Island.class})
class DiresightTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils two, draws two cards, and loses 2 life")
    void surveilsDrawsAndLosesLife() {
        Card surveilledCard = new BarkformHarvester();
        Card keptCard = new Island();
        Card drawnCard = new BarkformHarvester();
        Card secondDrawnCard = new Island();
        harness.setLibrary(player1, List.of(surveilledCard, keptCard, drawnCard, secondDrawnCard));
        harness.setHand(player1, List.of(new Diresight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gameData = harness.getGameData();
        int startingLife = gameData.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.Scry surveil = gameData.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(surveilledCard, keptCard);

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gameData.playerHands.get(player1.getId())).containsExactly(keptCard, drawnCard);
        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(secondDrawnCard);
        assertThat(gameData.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(2);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(surveilledCard);
        assertThat(gameData.getLife(player1.getId())).isEqualTo(startingLife - 2);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can keep both surveilled cards in either order before drawing")
    void keepsBothCardsInChosenOrder() {
        Card firstCard = new BarkformHarvester();
        Card secondCard = new Island();
        Card remainingCard = new Island();
        Diresight spell = new Diresight();
        harness.setLibrary(player1, List.of(firstCard, secondCard, remainingCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int startingLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCard, firstCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can put both surveilled cards into the graveyard and draw the next two")
    void putsBothCardsIntoGraveyard() {
        Card firstCard = new BarkformHarvester();
        Card secondCard = new Island();
        Card firstDraw = new Island();
        Card secondDraw = new BarkformHarvester();
        Diresight spell = new Diresight();
        harness.setLibrary(player1, List.of(firstCard, secondCard, firstDraw, secondDraw));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int startingLife = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCard, secondCard, spell);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
