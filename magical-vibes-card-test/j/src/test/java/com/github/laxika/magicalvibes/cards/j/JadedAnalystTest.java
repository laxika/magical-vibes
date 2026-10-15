package com.github.laxika.magicalvibes.cards.j;

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

@CardUsed({JadedAnalyst.class})
class JadedAnalystTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn removes defender and grants vigilance")
    void secondDrawRemovesDefenderAndGrantsVigilance() {
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new JadedAnalyst());
        harness.setLibrary(player1, List.of(new JadedAnalyst(), new JadedAnalyst(), new JadedAnalyst()));

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
        harness.setLibrary(player1, List.of(new JadedAnalyst(), new JadedAnalyst(), new JadedAnalyst(),
                new JadedAnalyst(), new JadedAnalyst()));

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

    @Test
    @DisplayName("The second draw triggers during the opponent's turn as well")
    void secondDrawDuringOpponentsTurnTriggers() {
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new JadedAnalyst());
        harness.setLibrary(player1, List.of(new JadedAnalyst(), new JadedAnalyst()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        drawCard(player1);
        assertThat(gd.stack).isEmpty();
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.VIGILANCE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, analyst, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The opponent's second draw does not trigger Jaded Analyst")
    void opponentsDrawsDoNotTrigger() {
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new JadedAnalyst());
        harness.setLibrary(player2, List.of(new JadedAnalyst(), new JadedAnalyst()));

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A draw before Jaded Analyst enters counts toward the second draw")
    void firstDrawBeforeEnteringCounts() {
        harness.setLibrary(player1, List.of(new JadedAnalyst(), new JadedAnalyst()));
        drawCard(player1);
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new JadedAnalyst());

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, analyst, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on the third draw")
    void enteringAfterSecondDrawDoesNotTrigger() {
        harness.setLibrary(player1, List.of(new JadedAnalyst(), new JadedAnalyst(), new JadedAnalyst()));
        drawCard(player1);
        drawCard(player1);
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new JadedAnalyst());

        drawCard(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, analyst, Keyword.VIGILANCE)).isFalse();
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
