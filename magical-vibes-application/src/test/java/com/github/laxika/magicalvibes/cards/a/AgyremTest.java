package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Agyrem.class, GrizzlyBears.class, YouthfulKnight.class})
class AgyremTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Agyrem(), gd.nextTimestamp()));
    }

    @Test
    void returnsWhiteCreaturesToTheirOwnersBattlefieldAtNextEndStep() {
        Permanent knight = addCreatureReady(player2, new YouthfulKnight());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, knight));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("Youthful Knight"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(
                card -> card.getName().equals("Youthful Knight"));
    }

    @Test
    void returnsNonwhiteCreaturesToTheirOwnersHandAtNextEndStep() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).hasSize(1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player2.getId())).anyMatch(
                card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(
                card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void chaosPreventsCreaturesFromAttackingTheControllerUntilPlaneswalk() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingAttacker = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, opposingAttacker, player2.getId())).isTrue();

        harness.inMutationScope(() -> planar.planeswalk(gd));

        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isTrue();
    }
}
