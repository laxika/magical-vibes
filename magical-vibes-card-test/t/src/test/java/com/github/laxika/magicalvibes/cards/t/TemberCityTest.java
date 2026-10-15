package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemberCity.class, Forest.class, GrizzlyBears.class})
class TemberCityTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TemberCity(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void tappingALandDealsDamageToItsController() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    void chaosMakesEachOpponentSacrificeANonlandPermanent() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentPermanent);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void otherPlayerTappingALandTakesDamageOnTheirTurn() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        gd.planechase.controllerId = player2.getId();
        harness.clearPriorityPassed();

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void eachLandTappedForManaTriggersSeparately() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);
        resolveAllTriggers();
        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    void opponentChoosesWhichNonlandPermanentToSacrificeAndKeepsLand() {
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(sacrificed.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kept, land).doesNotContain(sacrificed);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void chaosDoesNotSacrificeLandWhenOpponentHasNoNonlandPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chaosExcludesTheCurrentPlanarControllerAfterTurnChanges() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        gd.planechase.controllerId = player2.getId();
        harness.clearPriorityPassed();

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
