package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void castSnakeOfTheGoldenGrove() {
        harness.setHand(player1, List.of(new SnakeOfTheGoldenGrove()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
