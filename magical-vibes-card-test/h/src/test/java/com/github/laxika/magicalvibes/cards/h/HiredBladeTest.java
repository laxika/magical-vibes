package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiredBlade.class})
class HiredBladeTest extends BaseCardTest {

    @Test
    void canCastAndResolveDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new HiredBlade(), "{2}{B}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hired Blade");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastAndResolveDuringCombat() {
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new HiredBlade(), "{2}{B}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hired Blade");
    }

    @Test
    void canRespondToAnotherCreatureSpell() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new HiredBlade(), "{2}{B}");
        gs.passPriority(gd, player1);

        harness.castFromHand(player2, new HiredBlade(), "{2}{B}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Hired Blade");
        harness.assertNotOnBattlefield(player1, "Hired Blade");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hired Blade");
        assertThat(gd.stack).isEmpty();
    }
}
