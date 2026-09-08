package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiskyResearch.class, GrizzlyBears.class})
class RiskyResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils two, then draws two cards and loses 2 life")
    void surveilsDrawsAndLosesLife() {
        Card surveiledCardOne = new GrizzlyBears();
        Card surveiledCardTwo = new GrizzlyBears();
        Card drawnCardOne = new GrizzlyBears();
        Card drawnCardTwo = new GrizzlyBears();
        harness.setLibrary(player1, List.of(surveiledCardOne, surveiledCardTwo, drawnCardOne, drawnCardTwo));
        harness.setHand(player1, List.of(new RiskyResearch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveiledCardOne, surveiledCardTwo);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCardOne, drawnCardTwo);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}
