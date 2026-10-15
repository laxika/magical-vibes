package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlightDeckCoordinator.class, IntrepidTenderfoot.class, Plains.class})
class FlightDeckCoordinatorTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life at end step when controlling two tapped creatures")
    void gainsLifeWithTwoTappedCreatures() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        creature.tap();
        harness.addToBattlefield(player1, new IntrepidTenderfoot());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Only tapped creatures controlled by the coordinator's controller count")
    void opponentTappedCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
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
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
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
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        first.tap();
        second.tap();
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        second.untap();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The tapped coordinator counts toward its own condition")
    void coordinatorCountsItself() {
        Permanent coordinator = harness.addToBattlefieldAndReturn(player1, new FlightDeckCoordinator());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        coordinator.tap();
        other.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tapped noncreatures do not count toward the condition")
    void tappedLandDoesNotCount() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        creature.tap();
        land.tap();
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("More than two tapped creatures still gains only 2 life")
    void gainsOnlyTwoLifeWithThreeTappedCreatures() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot()).tap();
        }
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Meeting the condition after the end step begins does not trigger the ability")
    void conditionBecomingTrueLaterDoesNotTrigger() {
        harness.addToBattlefield(player1, new FlightDeckCoordinator());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        first.tap();
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
        second.tap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
