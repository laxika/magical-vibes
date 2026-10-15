package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AerialDoombot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RobotDomination.class, AerialDoombot.class})
class RobotDominationTest extends BaseCardTest {

    @Test
    @DisplayName("A creature card entering your graveyard draws, loses life, and adds a plan counter")
    void creatureCardEnteringGraveyardTriggers() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        Card drawn = new AerialDoombot();
        harness.setLibrary(player1, List.of(drawn));
        Permanent creature = addCreatureReady(player1, new AerialDoombot());
        int lifeBefore = gd.getLife(player1.getId());

        putIntoGraveyard(creature);
        resolveTopOfStack();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(domination.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }

    @Test
    @DisplayName("The third plan counter sacrifices Robot Domination and creates three Robots")
    void thirdPlanCounterCreatesRobots() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        domination.setCounterCount(CounterType.PLAN, 2);
        harness.setLibrary(player1, List.of(new AerialDoombot()));
        Permanent creature = addCreatureReady(player1, new AerialDoombot());

        putIntoGraveyard(creature);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Robot Domination");
        harness.assertInGraveyard(player1, "Robot Domination");
        assertRobots();
    }

    @Test
    @DisplayName("Robots are created even if Robot Domination leaves before its final ability resolves")
    void createsRobotsAfterSourceLeaves() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        domination.setCounterCount(CounterType.PLAN, 2);
        harness.setLibrary(player1, List.of(new AerialDoombot()));
        Permanent creature = addCreatureReady(player1, new AerialDoombot());

        putIntoGraveyard(creature);
        resolveTopOfStack();
        putIntoGraveyard(domination);
        resolveTopOfStack();

        assertRobots();
    }

    @Test
    @DisplayName("Removing plan counters after the third counter trigger does not prevent Robots")
    void createsRobotsAfterPlanCountersAreRemoved() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        domination.setCounterCount(CounterType.PLAN, 2);
        harness.setLibrary(player1, List.of(new AerialDoombot()));
        Permanent creature = addCreatureReady(player1, new AerialDoombot());

        putIntoGraveyard(creature);
        resolveTopOfStack();
        domination.setCounterCount(CounterType.PLAN, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Robot Domination");
        assertRobots();
    }

    @Test
    @DisplayName("Two creature cards dying simultaneously cause only one graveyard trigger")
    void simultaneousCreatureDeathsTriggerOnce() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        Card drawn = new AerialDoombot();
        harness.setLibrary(player1, List.of(drawn, new AerialDoombot(), new AerialDoombot()));
        Permanent first = addCreatureReady(player1, new AerialDoombot());
        Permanent second = addCreatureReady(player1, new AerialDoombot());
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player1, lifeBefore - 1);
        assertThat(domination.getCounterCount(CounterType.PLAN)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature card entering their graveyard does not trigger")
    void opponentsCreatureDoesNotTrigger() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        Permanent creature = addCreatureReady(player2, new AerialDoombot());

        putIntoGraveyard(creature);

        assertThat(gd.stack).isEmpty();
        assertThat(domination.getCounterCount(CounterType.PLAN)).isZero();
    }

    @Test
    @DisplayName("A noncreature card entering your graveyard does not trigger")
    void noncreatureCardDoesNotTrigger() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RobotDomination());

        putIntoGraveyard(other);

        assertThat(gd.stack).isEmpty();
        assertThat(domination.getCounterCount(CounterType.PLAN)).isZero();
    }

    @Test
    @DisplayName("Robot creature tokens dying do not trigger the graveyard ability")
    void creatureTokensDoNotTrigger() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        domination.setCounterCount(CounterType.PLAN, 2);
        harness.setLibrary(player1, List.of(new AerialDoombot()));
        Permanent creature = addCreatureReady(player1, new AerialDoombot());
        putIntoGraveyard(creature);
        resolveAllTriggers();
        assertRobots();
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new RobotDomination());

        putIntoGraveyard(findPermanent(player1, "Robot"));

        assertThat(gd.stack).isEmpty();
        assertThat(watcher.getCounterCount(CounterType.PLAN)).isZero();
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void assertRobots() {
        List<Permanent> robots = findPermanents(player1, "Robot");
        assertThat(robots).hasSize(3);
        assertThat(robots).allSatisfy(robot -> {
            assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(robot.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ROBOT, CardSubtype.VILLAIN);
            assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
        });
    }
}
