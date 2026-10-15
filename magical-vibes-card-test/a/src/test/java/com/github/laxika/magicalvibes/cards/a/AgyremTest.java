package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CardColor;
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
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Youthful Knight")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(
                card -> card.getName().equals("Youthful Knight"));
    }

    @Test
    void returnsNonwhiteCreaturesToTheirOwnersHandAtNextEndStep() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

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

    @Test
    void whiteReturnWaitsForTheDelayedTriggerToResolve() {
        Permanent knight = addCreatureReady(player2, new YouthfulKnight());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, knight));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(knight.getCard());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Youthful Knight")).isEqualTo(1);
    }

    @Test
    void nonwhiteReturnWaitsForTheDelayedTriggerToResolve() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(bears.getCard());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).contains(bears.getCard());
    }

    @Test
    void creatureThatWasGrantedWhiteReturnsToTheBattlefieldRatherThanHand() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.getGrantedColors().add(CardColor.WHITE);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(bears.getCard());
    }

    @Test
    void stolenCreaturesReturnToTheirOwnerEvenAfterPlaneswalkingAway() {
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        gd.stolenCreatures.put(knight.getId(), player2.getId());
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, knight);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears);
        });
        resolveAllTriggers();
        harness.inMutationScope(() -> planar.planeswalk(gd));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Youthful Knight")).isEqualTo(1);
        assertThat(countPermanents(player1, "Youthful Knight")).isZero();
        assertThat(gd.playerHands.get(player2.getId())).contains(bears.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears.getCard());
    }
}
