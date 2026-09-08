package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlertHeedbonder.class, GrizzlyBears.class, SerraAngel.class})
class AlertHeedbonderTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each vigilant creature you control")
    void gainsLifeForEachVigilantCreatureYouControl() {
        harness.addToBattlefield(player1, new AlertHeedbonder());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Counts vigilant creatures when the ability resolves")
    void countsVigilantCreaturesAtResolution() {
        harness.addToBattlefield(player1, new AlertHeedbonder());
        harness.setLife(player1, 10);

        advanceToEndStep(player1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Triggers only at the controller's end step")
    void triggersOnlyAtControllerEndStep() {
        harness.addToBattlefield(player1, new AlertHeedbonder());
        harness.setLife(player1, 10);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }
}
