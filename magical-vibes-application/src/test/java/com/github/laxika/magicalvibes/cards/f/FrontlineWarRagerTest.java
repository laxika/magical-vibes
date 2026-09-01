package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrontlineWarRager.class, GrizzlyBears.class})
class FrontlineWarRagerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself with two tapped creatures")
    void putsCounterWithTwoTappedCreatures() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedBear(player1);
        addTappedBear(player1);

        advanceToEndStep(player1);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself with fewer than two tapped creatures")
    void doesNotPutCounterWithFewerThanTwoTappedCreatures() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedBear(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts only tapped creatures controlled by its controller")
    void countsOnlyControlledTappedCreatures() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedBear(player2);
        addTappedBear(player2);

        advanceToEndStep(player1);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addTappedBear(Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bear.tap();
        return bear;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
