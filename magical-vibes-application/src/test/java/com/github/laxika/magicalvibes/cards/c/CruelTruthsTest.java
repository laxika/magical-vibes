package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CruelTruths.class, GrizzlyBears.class, Island.class})
class CruelTruthsTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils two, draws two cards, and causes its controller to lose 2 life")
    void surveilsDrawsAndLosesLife() {
        Card surveilledCard = new GrizzlyBears();
        Card keptCard = new Island();
        Card drawnCard = new GrizzlyBears();
        Card secondDrawnCard = new Island();
        harness.setLibrary(player1, List.of(surveilledCard, keptCard, drawnCard, secondDrawnCard));
        harness.setHand(player1, List.of(new CruelTruths()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        GameData gameData = harness.getGameData();
        int startingLife = gameData.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gameData.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(surveilledCard, keptCard);

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gameData.playerHands.get(player1.getId())).containsExactly(keptCard, drawnCard);
        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(secondDrawnCard);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(surveilledCard);
        harness.assertLife(player1, startingLife - 2);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }
}
