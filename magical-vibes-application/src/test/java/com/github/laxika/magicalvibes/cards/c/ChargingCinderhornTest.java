package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChargingCinderhorn.class, GrizzlyBears.class})
class ChargingCinderhornTest extends BaseCardTest {

    @Test
    void addsFuryCounterAndDealsThatMuchDamageToTheActiveEndStepPlayer() {
        Permanent cinderhorn = harness.addToBattlefieldAndReturn(player1, new ChargingCinderhorn());

        advanceToEndStep(player2);

        assertThat(cinderhorn.getCounterCount(CounterType.FURY)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void damageUsesTheUpdatedFuryCounterCount() {
        Permanent cinderhorn = harness.addToBattlefieldAndReturn(player1, new ChargingCinderhorn());
        cinderhorn.setCounterCount(CounterType.FURY, 2);

        advanceToEndStep(player2);

        assertThat(cinderhorn.getCounterCount(CounterType.FURY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void doesNotTriggerIfACreatureAttackedThisTurn() {
        Permanent cinderhorn = harness.addToBattlefieldAndReturn(player1, new ChargingCinderhorn());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));

        advanceToEndStep(player2);

        assertThat(cinderhorn.getCounterCount(CounterType.FURY)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
