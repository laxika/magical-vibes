package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GoblinGame.class)
class GoblinGameTest extends BaseCardTest {

    @Test
    void eachPlayerMustHideAtLeastOneItem() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castGoblinGame();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and");

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 1);

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 9);
    }

    @Test
    void eachPlayerLosesTheirCountThenFewestLosesHalfRoundedUp() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castGoblinGame();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 2);

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tiedFewestPlayersEachLoseHalfTheirLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castGoblinGame();

        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 2);

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 9);
    }

    @Test
    void negativeLifeTotalsAreNotChangedByTheFewestPlayersHalf() {
        harness.setLife(player1, 1);
        harness.setLife(player2, 20);

        castGoblinGame();

        harness.handleXValueChosen(player1, 4);
        harness.handleXValueChosen(player2, 5);

        harness.assertLife(player1, -3);
        harness.assertLife(player2, 15);
    }

    private void castGoblinGame() {
        harness.castFromHand(player1, new GoblinGame(), "{5}{R}{R}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
    }
}
