package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrimsonHonorGuard.class, GrizzlyBears.class})
class CrimsonHonorGuardTest extends BaseCardTest {

    @Test
    void dealsFourDamageAtEachPlayersEndStepWithoutACommander() {
        addCreatureReady(player1, new CrimsonHonorGuard());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        advanceToEndStep(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void doesNotDamageAnEndStepPlayerWhoControlsACommander() {
        addCreatureReady(player1, new CrimsonHonorGuard());
        Permanent commander = addCreatureReady(player2, new GrizzlyBears());
        gd.makeCommander(player2.getId(), commander.getCard());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player2);
        advanceToEndStep(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
