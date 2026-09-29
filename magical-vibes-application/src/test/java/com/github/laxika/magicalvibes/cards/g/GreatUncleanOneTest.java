package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GreatUncleanOne.class)
class GreatUncleanOneTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, each opponent loses 2 life and a lower-life opponent creates a Plaguebearer")
    void losesLifeThenCreatesTokenForLowerLifeOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GreatUncleanOne());

        runToEndStep();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Great Unclean One", "Plaguebearer of Nurgle");
    }

    @Test
    @DisplayName("Does not create a token for an opponent who is not lower life after the loss")
    void noTokenForOpponentStillAtLeastAsMuchLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 22);
        harness.addToBattlefield(player1, new GreatUncleanOne());

        runToEndStep();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Great Unclean One");
    }

    private void runToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
