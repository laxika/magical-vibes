package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndulgingPatricianTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each opponent loses 3 life at your end step after gaining at least 3 life")
    void eachOpponentLosesLifeAtThreshold() {
        harness.addToBattlefield(player1, new IndulgingPatrician());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        int startingLife = gd.getLife(player2.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 3);
    }

    @Test
    @DisplayName("Does not trigger when fewer than 3 life was gained")
    void doesNotTriggerBelowThreshold() {
        harness.addToBattlefield(player1, new IndulgingPatrician());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        int startingLife = gd.getLife(player2.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Triggers only during its controller's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new IndulgingPatrician());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        int startingLife = gd.getLife(player2.getId());

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }
}
