package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimeMinistersCabinetRoom.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class PrimeMinistersCabinetRoomTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new PrimeMinistersCabinetRoom(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void beginningOfCombatCopiesAnOpponentsCreatureOntoAnOptionalCreatureYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void canDeclineTheCreatureYouControlTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void chaosVotesOnlyForOpponentsCreaturesAndExilesTheMostVotedCreature() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(firstCreature.getId(), secondCreature.getId());
        harness.handleMultiplePermanentsChosen(player1, java.util.List.of(firstCreature.getId()));
        harness.handleMultiplePermanentsChosen(player2, java.util.List.of(firstCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(secondCreature, land)
                .doesNotContain(firstCreature);
    }
}
