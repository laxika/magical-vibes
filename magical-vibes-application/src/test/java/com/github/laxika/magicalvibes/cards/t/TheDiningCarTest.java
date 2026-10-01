package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheDiningCar.class, GrizzlyBears.class, AirElemental.class})
class TheDiningCarTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkMakesEachPlayerCreateFood() {
        gd.planechase.deck.add(new TheDiningCar());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player2, "Food")).isOne();
    }

    @Test
    void upkeepSacrificesLeastToughnessCreatureThenInvestigates() {
        gd.planechase.deck.add(new TheDiningCar());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        Permanent leastToughness = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent larger = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.forceStep(TurnStep.UPKEEP);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(larger);
        assertThat(countPermanents(player1, "Clue")).isOne();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(leastToughness);
    }

    @Test
    void upkeepLetsControllerChooseAmongTiedLeastToughnessCreatures() {
        gd.planechase.faceUp.add(new com.github.laxika.magicalvibes.model.planar.PlanarObject(
                new TheDiningCar(), gd.nextTimestamp()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceStep(TurnStep.UPKEEP);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first).contains(second);
        assertThat(countPermanents(player1, "Clue")).isOne();
    }

    @Test
    void chaosReducesArtifactTokenActivatedAbilitiesUntilEndOfTurn() {
        gd.planechase.deck.add(new TheDiningCar());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();
        Permanent food = findPermanent(player1, "Food");

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);

        assertThat(gd.stack).hasSize(1);
    }
}
