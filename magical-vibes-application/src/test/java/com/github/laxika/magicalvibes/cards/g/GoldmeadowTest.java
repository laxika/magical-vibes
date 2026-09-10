package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({Goldmeadow.class, Forest.class})
class GoldmeadowTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Goldmeadow(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void ownLandCreatesThreeGoatsForItsController() {
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(3);
        assertThat(findPermanents(player2, "Goat")).isEmpty();
    }

    @Test
    void opponentsLandCreatesThreeGoatsForThatOpponent() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).isEmpty();
        assertThat(findPermanents(player2, "Goat")).hasSize(3);
    }

    @Test
    void chaosCreatesOneGoatForPlanarController() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(1);
        assertThat(findPermanents(player2, "Goat")).isEmpty();
    }
}
