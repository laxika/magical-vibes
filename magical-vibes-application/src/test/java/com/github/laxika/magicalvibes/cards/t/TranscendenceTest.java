package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Transcendence.class, Shock.class})
class TranscendenceTest extends BaseCardTest {

    @Test
    @DisplayName("Controller does not lose for having 0 or less life")
    void controllerDoesNotLoseAtZeroOrLessLife() {
        harness.addToBattlefield(player1, new Transcendence());
        harness.setLife(player1, 1);

        castShockAtPlayer1(false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Losing life makes the controller gain twice that amount")
    void losingLifeGainsTwiceTheAmount() {
        harness.addToBattlefield(player1, new Transcendence());
        harness.setLife(player1, 10);

        castShockAtPlayer1(true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
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

        castShockAtPlayer1(true);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void castShockAtPlayer1(boolean resolveLifeGainTrigger) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        if (resolveLifeGainTrigger) {
            harness.passBothPriorities();
        }
    }
}
