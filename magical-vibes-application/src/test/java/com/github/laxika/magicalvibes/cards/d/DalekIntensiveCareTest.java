package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

@CardUsed({DalekIntensiveCare.class, GrizzlyBears.class, AirElemental.class})
class DalekIntensiveCareTest extends BaseCardTest {

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
    void planeswalkAndUpkeepExileOnlyNonDalekCreaturesAndCreateHastyDaleks() {
        addCreatureReady(player1, new GrizzlyBears());
        gd.planechase.deck.add(new DalekIntensiveCare());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dalek")).isOne();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceStep(TurnStep.UPKEEP);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second);
        assertThat(countPermanents(player1, "Dalek")).isEqualTo(2);
        Permanent created = findPermanents(player1, "Dalek").getLast();
        assertThat(gqs.hasKeyword(gd, created, com.github.laxika.magicalvibes.model.Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, created, com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
    }

    @Test
    void chaosDalekDealsItsPowerToAnOpposingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        gd.planechase.deck.add(new DalekIntensiveCare());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();
        Permanent dalek = findPermanent(player1, "Dalek");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextETBTokenMultiTargetTrigger(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(dalek.getId());
        harness.handlePermanentChosen(player1, dalek.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }
}
