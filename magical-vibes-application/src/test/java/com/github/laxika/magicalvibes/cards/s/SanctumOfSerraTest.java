package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctumOfSerra.class, Forest.class, GrizzlyBears.class, ZuranOrb.class})
class SanctumOfSerraTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new SanctumOfSerra(), gd.nextTimestamp()));
    }

    @Test
    void planeswalkingAwayDestroysAllNonlandPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Zuran Orb");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void chaosMaySetThePlanarControllersLifeToTwenty() {
        harness.setLife(player1, 10);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
    }

    @Test
    void chaosMayBeDeclined() {
        harness.setLife(player1, 10);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 10);
    }

    @Test
    void chaosCanLowerLifeToTwenty() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 12);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
    }

    @Test
    void chaosUsesTheCurrentPlanarController() {
        gd.planechase.controllerId = player2.getId();
        gd.activePlayerId = player2.getId();
        harness.setLife(player1, 8);
        harness.setLife(player2, 14);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 8);
        harness.assertLife(player2, 20);
    }

    @Test
    void departingTriggerDestroysPermanentsPresentAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.planeswalk(gd));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Zuran Orb");
    }
}
