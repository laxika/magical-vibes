package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnakeOfTheGoldenGrove.class})
class SnakeOfTheGoldenGroveTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent pays tribute and Snake of the Golden Grove enters with three +1/+1 counters")
    void opponentPaysTribute() {
        harness.setLife(player1, 10);
        castSnakeOfTheGoldenGrove();

        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player1, true))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMayAbilityChosen(player2, true);

        Permanent snake = findPermanent(player1, "Snake of the Golden Grove");
        assertThat(snake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Declining tribute causes its controller to gain four life")
    void opponentDeclinesTribute() {
        harness.setLife(player1, 10);
        castSnakeOfTheGoldenGrove();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(findPermanent(player1, "Snake of the Golden Grove")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Paying tribute does not put a life-gain ability on the stack")
    void paidTributeDoesNotTriggerLifeGain() {
        harness.setLife(player1, 10);
        castSnakeOfTheGoldenGrove();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining tribute creates a life-gain trigger that resolves separately")
    void declinedTributeUsesTheStack() {
        harness.setLife(player1, 10);
        castSnakeOfTheGoldenGrove();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The opposing controller gains life when their opponent declines tribute")
    void opposingControllerGainsLife() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 10);
        castSnakeOfTheGoldenGrove(player2);

        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player2, false))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 8);
        harness.assertLife(player2, 14);
        assertThat(findPermanent(player2, "Snake of the Golden Grove")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castSnakeOfTheGoldenGrove() {
        castSnakeOfTheGoldenGrove(player1);
    }

    private void castSnakeOfTheGoldenGrove(Player controller) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(controller, new SnakeOfTheGoldenGrove(), "{4}{G}");
        harness.passBothPriorities();
    }
}
