package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHippodrome.class, CrawWurm.class, GrizzlyBears.class})
class TheHippodromeTest extends BaseCardTest {
    private PlanechaseService planar;

    @BeforeEach
    void setupPlanarState() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        faceUp(new TheHippodrome());
    }

    @Test
    void allCreaturesGetMinusFivePower() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(-3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(-3);
    }

    @Test
    void chaosMayDestroyCreatureWithPowerZeroOrLess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChaosAndChooseTarget(target);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void chaosDoesNotDestroyCreatureWhosePowerIsAboveZeroAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        triggerChaosAndChooseTarget(target);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void triggerChaosAndChooseTarget(Permanent target) {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
    }

    private PlanarObject faceUp(Card card) {
        PlanarObject object = new PlanarObject(card, gd.nextTimestamp());
        gd.planechase.faceUp.add(object);
        return object;
    }
}
