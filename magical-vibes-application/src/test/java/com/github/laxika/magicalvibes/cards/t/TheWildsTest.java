package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWilds.class, GrizzlyBears.class, AirElemental.class, TheMasterMultiplied.class})
class TheWildsTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void setupPlanarState() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkAndUpkeepCreateFood() {
        gd.planechase.deck.add(new TheWilds());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Food")).isOne();

        harness.forceStep(TurnStep.UPKEEP);
        GameTestEngineContext.get().getBean(StepTriggerService.class).handleUpkeepTriggers(gd);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    void chaosSacrificesTargetPlayersSmallCreatureAndCreatesFoodForController() {
        gd.planechase.faceUp.add(new PlanarObject(new TheWilds(), gd.nextTimestamp()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        triggerChaosTargetingPlayer(player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player2, "Food")).isZero();
    }

    @Test
    void chaosLetsTargetPlayerChooseLargeCreatureForTwoFood() {
        gd.planechase.faceUp.add(new PlanarObject(new TheWilds(), gd.nextTimestamp()));
        var smallCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var largeCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        triggerChaosTargetingPlayer(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(
                        smallCreature.getId(), largeCreature.getId());
        harness.handlePermanentChosen(player2, largeCreature.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void chaosCreatesNoFoodWhenTargetPlayerHasNoCreatures() {
        gd.planechase.faceUp.add(new PlanarObject(new TheWilds(), gd.nextTimestamp()));

        triggerChaosTargetingPlayer(player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player2, "Food")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chaosCanTargetControllerAndSacrificeTheirCreature() {
        gd.planechase.faceUp.add(new PlanarObject(new TheWilds(), gd.nextTimestamp()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        triggerChaosTargetingPlayer(player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player2, "Food")).isZero();
    }

    @Test
    void chaosUsesToughnessIncludingPositiveCountersBeforeSacrifice() {
        gd.planechase.faceUp.add(new PlanarObject(new TheWilds(), gd.nextTimestamp()));
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        triggerChaosTargetingPlayer(player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    void chaosUsesReducedToughnessInsteadOfPrintedToughness() {
        gd.planechase.faceUp.add(new PlanarObject(new TheWilds(), gd.nextTimestamp()));
        var creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        triggerChaosTargetingPlayer(player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    void upkeepCreatesFoodForNewActivePlayer() {
        gd.planechase.faceUp.add(new PlanarObject(new TheWilds(), gd.nextTimestamp()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player2, "Food")).isOne();
    }

    @Test
    void chaosCreatesNoFoodWhenControlledTriggerCannotSacrificeCreatureToken() {
        gd.planechase.faceUp.add(new PlanarObject(new TheWilds(), gd.nextTimestamp()));
        TheMasterMultiplied tokenCopy = new TheMasterMultiplied();
        tokenCopy.setToken(true);
        var token = harness.addToBattlefieldAndReturn(player1, tokenCopy);

        triggerChaosTargetingPlayer(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private void triggerChaosTargetingPlayer(java.util.UUID targetPlayerId) {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, targetPlayerId);
    }
}
