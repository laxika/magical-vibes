package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlightDeckCoordinator.class, GrizzlyBears.class})
class FlightDeckCoordinatorTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life at end step when controlling two tapped creatures")
    void gainsLifeWithTwoTappedCreatures() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not trigger with fewer than two tapped creatures")
    void doesNotTriggerWithFewerThanTwoTappedCreatures() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Only tapped creatures controlled by the coordinator's controller count")
    void opponentTappedCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.setLife(player1, 20);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Intervening-if fails if the second tapped creature untaps before resolution")
    void interveningIfFailsAtResolution() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        second.untap();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
