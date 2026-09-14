package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Transcendence.class, FieryTemper.class})
class TranscendenceTest extends BaseCardTest {

    @Test
    @DisplayName("Controller does not lose for having 0 or less life")
    void controllerDoesNotLoseAtZeroOrLessLife() {
        harness.addToBattlefield(player1, new Transcendence());
        harness.setLife(player1, 1);

        castFieryTemperAtPlayer1(false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(-2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Losing life makes the controller gain twice that amount")
    void losingLifeGainsTwiceTheAmount() {
        harness.addToBattlefield(player1, new Transcendence());
        harness.setLife(player1, 10);

        castFieryTemperAtPlayer1(true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Controller loses when the state trigger sees 20 or more life")
    void controllerLosesAtTwentyLife() {
        harness.addToBattlefield(player1, new Transcendence());
        harness.setLife(player1, 20);

        harness.runStateBasedActions();
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Life gain to 20 or more life causes the state-triggered loss")
    void lifeGainToTwentyCausesLoss() {
        harness.addToBattlefield(player1, new Transcendence());
        harness.setLife(player1, 19);

        castFieryTemperAtPlayer1(true);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent life loss does not trigger Transcendence")
    void opponentLifeLossDoesNotTrigger() {
        harness.addToBattlefield(player1, new Transcendence());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void castFieryTemperAtPlayer1(boolean resolveLifeGainTrigger) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        if (resolveLifeGainTrigger) {
            harness.passBothPriorities();
        }
    }
}
