package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IvorytuskFortressTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps creatures with +1/+1 counters during an opponent's untap step")
    void untapsCreaturesWithPlusOneCountersDuringOpponentsUntapStep() {
        Permanent fortress = addCreatureReady(player1, new IvorytuskFortress());
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ageCounterCreature = addCreatureReady(player1, new HillGiant());
        Permanent uncounteredCreature = addCreatureReady(player1, new HillGiant());

        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        ageCounterCreature.setCounterCount(CounterType.AGE, 1);
        fortress.tap();
        counteredCreature.tap();
        ageCounterCreature.tap();
        uncounteredCreature.tap();

        advanceToNextTurn(player1);

        assertThat(fortress.isTapped()).isTrue();
        assertThat(counteredCreature.isTapped()).isFalse();
        assertThat(ageCounterCreature.isTapped()).isTrue();
        assertThat(uncounteredCreature.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
