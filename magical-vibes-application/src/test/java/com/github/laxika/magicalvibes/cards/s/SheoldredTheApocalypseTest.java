package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SheoldredTheApocalypse.class, GrizzlyBears.class})
class SheoldredTheApocalypseTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains 2 life when drawing a card")
    void controllerDrawTriggersLifeGain() {
        harness.addToBattlefield(player1, new SheoldredTheApocalypse());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Opponent loses 2 life when drawing a card")
    void opponentDrawTriggersLifeLoss() {
        harness.addToBattlefield(player1, new SheoldredTheApocalypse());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
