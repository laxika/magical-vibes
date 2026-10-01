package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMatrixOfTime.class, DarkRitual.class, Forest.class})
class TheMatrixOfTimeTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkingToTheMatrixExilesAndTracksEachTopCard() {
        Card ownTop = new Forest();
        ownTop.setOwnerId(player1.getId());
        Card opposingTop = new Forest();
        opposingTop.setOwnerId(player2.getId());
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opposingTop));

        revealMatrix();

        var sourceId = gd.planechase.faceUp.getFirst().getId();
        assertThat(gd.findExiledCard(ownTop.getId()).sourcePermanentId()).isEqualTo(sourceId);
        assertThat(gd.findExiledCard(opposingTop.getId()).sourcePermanentId()).isEqualTo(sourceId);
    }

    @Test
    void controllerMayCastOpponentsTrackedCardAndItsOwnerLosesLife() {
        Card ownTop = new Forest();
        ownTop.setOwnerId(player1.getId());
        Card opposingSpell = new DarkRitual();
        opposingSpell.setOwnerId(player2.getId());
        Card nextOpposingTop = new Forest();
        nextOpposingTop.setOwnerId(player2.getId());
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opposingSpell, nextOpposingTop));
        revealMatrix();

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        int opposingLife = gd.getLife(player2.getId());
        harness.castFromExile(player1, opposingSpell.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opposingLife - 3);
        assertThat(gd.findExiledCard(nextOpposingTop.getId())).isNotNull();
        assertThat(gd.findExiledCard(nextOpposingTop.getId()).sourcePermanentId()).isNull();
    }

    @Test
    void chaosCreatesTwoTreasures() {
        revealMatrix();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    private void revealMatrix() {
        harness.inMutationScope(() -> planar.completePlaneswalkToPlaneWithoutDeparting(
                gd, new TheMatrixOfTime(), List.of(), player1.getId()));
        harness.passBothPriorities();
    }
}
