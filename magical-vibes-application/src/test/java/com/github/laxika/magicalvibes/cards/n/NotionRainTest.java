package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DimirInformant;
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

@CardUsed({NotionRain.class, DimirInformant.class})
class NotionRainTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils two, draws two cards, and deals 2 damage to its controller")
    void surveilsDrawsAndDealsDamage() {
        Card surveilledCard = new DimirInformant();
        Card keptCard = new NotionRain();
        Card drawnCard = new DimirInformant();
        Card secondDrawnCard = new NotionRain();
        harness.setLibrary(player1, List.of(surveilledCard, keptCard, drawnCard, secondDrawnCard));
        harness.setHand(player1, List.of(new NotionRain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        GameData gameData = harness.getGameData();
        int startingLife = gameData.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.Scry surveil = gameData.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(surveilledCard, keptCard);

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gameData.playerHands.get(player1.getId())).containsExactly(keptCard, drawnCard);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(surveilledCard);
        harness.assertLife(player1, startingLife - 2);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Keeping both surveilled cards allows reordering before drawing them")
    void keepsBothCardsInChosenOrder() {
        Card first = new DimirInformant();
        Card second = new NotionRain();
        Card remaining = new NotionRain();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new NotionRain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card == first || card == second);
        harness.assertInGraveyard(player1, "Notion Rain");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Putting both surveilled cards into the graveyard draws the next two cards")
    void putsBothCardsIntoGraveyard() {
        Card first = new DimirInformant();
        Card second = new NotionRain();
        Card third = new NotionRain();
        Card fourth = new DimirInformant();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new NotionRain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Notion Rain");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
