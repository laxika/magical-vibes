package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RobotDomination.class, GrizzlyBears.class})
class RobotDominationTest extends BaseCardTest {

    @Test
    @DisplayName("A creature card entering your graveyard draws, loses life, and adds a plan counter")
    void creatureCardEnteringGraveyardTriggers() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        putIntoGraveyard(creature);
        resolveTopOfStack();
        resolveTopOfStack();

        harness.assertNotOnBattlefield(player1, "Robot Domination");
        harness.assertInGraveyard(player1, "Robot Domination");
        assertRobots();
    }

    @Test
    @DisplayName("Robots are created even if Robot Domination leaves before its final ability resolves")
    void createsRobotsAfterSourceLeaves() {
        Permanent domination = harness.addToBattlefieldAndReturn(player1, new RobotDomination());
        domination.setCounterCount(CounterType.PLAN, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        putIntoGraveyard(creature);
        resolveTopOfStack();
        putIntoGraveyard(domination);
        resolveTopOfStack();

        assertRobots();
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
