package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonsDue.class, CivicGardener.class})
class DemonsDueTest extends BaseCardTest {

    @Test
    @DisplayName("Scries two, draws two cards, and causes its controller to lose 2 life")
    void scriesDrawsAndLosesLife() {
        Card bottomCard = new CivicGardener();
        Card keptCard = new CivicGardener();
        Card drawnCard = new CivicGardener();
        Card secondDrawnCard = new CivicGardener();
        Card spell = new DemonsDue();
        harness.setLibrary(player1, List.of(bottomCard, keptCard, drawnCard, secondDrawnCard));

        GameData gameData = harness.getGameData();
        int startingLife = gameData.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, spell, "{3}{B}");
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gameData.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(bottomCard, keptCard);

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gameData.playerHands.get(player1.getId())).containsExactly(keptCard, drawnCard);
        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(secondDrawnCard, bottomCard);
        assertThat(gameData.playerGraveyards.get(player1.getId())).containsExactly(spell);
        harness.assertLife(player1, startingLife - 2);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draw and life loss wait for scry, then use the chosen top order")
    void reordersBothCardsBeforeDrawing() {
        Card first = new CivicGardener();
        Card second = new CivicGardener();
        Card remaining = new CivicGardener();
        Card spell = new DemonsDue();
        harness.setLibrary(player1, List.of(first, second, remaining));
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        List<Card> opponentHand = List.copyOf(gd.playerHands.get(player2.getId()));
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));

        harness.castFromHand(player1, spell, "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, startingLife);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        harness.assertLife(player1, startingLife - 2);
        harness.assertLife(player2, opponentLife);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Putting both scry cards on the bottom draws the next two cards")
    void bottomsBothCardsBeforeDrawing() {
        Card first = new CivicGardener();
        Card second = new CivicGardener();
        Card third = new CivicGardener();
        Card fourth = new CivicGardener();
        Card spell = new DemonsDue();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, spell, "{3}{B}");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        harness.assertLife(player1, startingLife - 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
