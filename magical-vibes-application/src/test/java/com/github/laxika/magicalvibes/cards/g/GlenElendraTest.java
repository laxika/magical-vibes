package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlenElendra.class, GrizzlyBears.class})
class GlenElendraTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new GlenElendra(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
    }

    @Test
    void exchangesTargetCreaturesWhenAccepted() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        gd.combatDamageToPlayersThisCombat
                .computeIfAbsent(first.getId(), ignored -> new java.util.HashSet<>())
                .add(player2.getId());

        triggerEndOfCombat();

        harness.handlePermanentChosen(player1, first.getId());
        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.validPermanentIds()).containsExactly(second.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first);
    }

    @Test
    void onlyPlayersDealtCombatDamageByFirstTargetCanSupplySecondTarget() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        gd.combatDamageToPlayersThisCombat
                .computeIfAbsent(first.getId(), ignored -> new java.util.HashSet<>())
                .add(player2.getId());

        triggerEndOfCombat();
        harness.handlePermanentChosen(player1, first.getId());

        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.validPermanentIds()).containsExactly(opponentCreature.getId());
        assertThat(secondChoice.validPermanentIds()).doesNotContain(ownCreature.getId());
    }

    @Test
    void chaosGainsControlOfTargetCreatureYouOwn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player1.getId());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void decliningExchangeLeavesBothControllersUnchanged() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        gd.combatDamageToPlayersThisCombat
                .computeIfAbsent(first.getId(), ignored -> new java.util.HashSet<>())
                .add(player2.getId());

        triggerEndOfCombat();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void exchangeDoesNothingWhenSecondTargetLeavesBeforeResolution() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        gd.combatDamageToPlayersThisCombat
                .computeIfAbsent(first.getId(), ignored -> new java.util.HashSet<>())
                .add(player2.getId());

        triggerEndOfCombat();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        gd.playerBattlefields.get(player2.getId()).remove(second);
        gd.playerGraveyards.get(player2.getId()).add(second.getCard());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first);
    }

    @Test
    void chaosOnlyOffersCreaturesOwnedByThePlanarController() {
        Permanent stolenOwnCreature = addCreatureReady(player2, new GrizzlyBears());
        gd.stolenCreatures.put(stolenOwnCreature.getId(), player1.getId());
        Permanent opponentsCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent stolenOpponentsCreature = addCreatureReady(player1, new GrizzlyBears());
        gd.stolenCreatures.put(stolenOpponentsCreature.getId(), player2.getId());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrder(stolenOwnCreature.getId(), ownCreature.getId())
                .doesNotContain(opponentsCreature.getId(), stolenOpponentsCreature.getId());
        harness.handlePermanentChosen(player1, stolenOwnCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolenOwnCreature);
    }

    private void triggerEndOfCombat() {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleEndOfCombatTriggers(gd));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
    }
}
