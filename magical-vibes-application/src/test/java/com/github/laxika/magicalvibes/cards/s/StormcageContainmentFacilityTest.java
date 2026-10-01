package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormcageContainmentFacility.class, GrizzlyBears.class, RaptorHatchling.class, Shock.class})
class StormcageContainmentFacilityTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new StormcageContainmentFacility(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void givesCreatureCardsInControllersGraveyardEscape() {
        harness.setGraveyard(player1, List.of(
                new RaptorHatchling(), new RaptorHatchling(), new RaptorHatchling(), new RaptorHatchling()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raptor Hatchling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    void doesNotGiveEscapeToNoncreatureCards() {
        harness.setGraveyard(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chaosDetainsTargetCreatureAnOpponentControlsUntilControllersNextTurn() {
        var opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opponentCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLockedFromAttacking(gd, opponentCreature.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, opponentCreature.getId())).isTrue();
        assertThat(gqs.isLockedFromActivatingAbilities(gd, opponentCreature.getId())).isTrue();
        assertThat(gqs.isLockedFromAttacking(gd, ownCreature.getId())).isFalse();

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThat(gqs.isLockedFromAttacking(gd, opponentCreature.getId())).isFalse();
    }
}
