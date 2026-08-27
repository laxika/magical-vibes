package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadedAnalyst.class, GrizzlyBears.class})
class JadedAnalystTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn removes defender and grants vigilance")
    void secondDrawRemovesDefenderAndGrantsVigilance() {
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new JadedAnalyst());
        setDeck(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player1);
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.VIGILANCE)).isFalse();

        drawCard(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, analyst, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The draw trigger fires only once per turn and its changes expire at end of turn")
    void triggerFiresOncePerTurnAndResetsAtEndOfTurn() {
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new JadedAnalyst());
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player1);
        drawCard(player1);
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, analyst, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.VIGILANCE)).isFalse();
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
