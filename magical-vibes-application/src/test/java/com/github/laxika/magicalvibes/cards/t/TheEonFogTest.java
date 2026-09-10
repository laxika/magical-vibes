package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CityOfSolitude;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

@CardUsed({CityOfSolitude.class, Forest.class, GrizzlyBears.class, TheEonFog.class})
class TheEonFogTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheEonFog(), gd.nextTimestamp()));
    }

    @Test
    void playersSkipTheirUntapSteps() {
        Permanent playerOnePermanent = harness.addToBattlefieldAndReturn(player1, new CityOfSolitude());
        Permanent playerTwoPermanent = harness.addToBattlefieldAndReturn(player2, new CityOfSolitude());
        playerOnePermanent.tap();
        playerTwoPermanent.tap();

        advanceToNextTurn(player1);
        assertThat(playerTwoPermanent.isTapped()).isTrue();

        advanceToNextTurn(player2);
        assertThat(playerOnePermanent.isTapped()).isTrue();
    }

    @Test
    void chaosUntapsAllPermanentsControlledByThePlanarController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new CityOfSolitude());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        land.tap();
        enchantment.tap();
        opponentPermanent.tap();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(enchantment.isTapped()).isFalse();
        assertThat(opponentPermanent.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player nextActivePlayer = currentActivePlayer.getId().equals(player1.getId()) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
