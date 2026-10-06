package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PouncingCheetah.class})
class PouncingCheetahTest extends BaseCardTest {

    @Test
    void canCastAndResolveDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new PouncingCheetah(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pouncing Cheetah");
        harness.assertNotOnBattlefield(player2, "Pouncing Cheetah");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastAndResolveDuringCombat() {
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new PouncingCheetah(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pouncing Cheetah");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canRespondToAnotherCreatureSpell() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new PouncingCheetah(), "{2}{G}");

        harness.castFromHand(player2, new PouncingCheetah(), "{2}{G}");

        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.assertOnBattlefield(player2, "Pouncing Cheetah");
        harness.assertNotOnBattlefield(player1, "Pouncing Cheetah");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pouncing Cheetah");
        assertThat(gd.stack).isEmpty();
    }
}
