package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwoStreamsFacility.class, GrizzlyBears.class})
class TwoStreamsFacilityTest extends BaseCardTest {

    @Test
    void playersChoicesGrantTheirCorrespondingBenefits() {
        PlanechaseService planar = setupPlane();
        var ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PlanarObject source = gd.planechase.faceUp.getFirst();

        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Green anchor");
        harness.handleListChoice(player2, "Red waterfall");

        assertThat(source.getChosenModeByPlayer()).containsEntry(player1.getId(), "Green anchor")
                .containsEntry(player2.getId(), "Red waterfall");
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.HASTE)).isTrue();
        assertThat(gqs.getMaxLandsThisTurn(gd, player1.getId())).isEqualTo(2);
        assertThat(gqs.getMaxLandsThisTurn(gd, player2.getId())).isEqualTo(1);
    }

    @Test
    void chaosSwitchesExistingChoicesAndTheUpkeepChoiceOnlyHappensOnTheFirstUpkeep() {
        PlanechaseService planar = setupPlane();
        PlanarObject source = gd.planechase.faceUp.getFirst();
        source.getChosenModeByPlayer().put(player1.getId(), "Green anchor");
        source.getChosenModeByPlayer().put(player2.getId(), "Red waterfall");

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        assertThat(source.getChosenModeByPlayer()).containsEntry(player1.getId(), "Red waterfall")
                .containsEntry(player2.getId(), "Green anchor");

        source.getChosenModeByPlayer().clear();
        gd.turnNumber = 2;
        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.UPKEEP_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(source.getChosenModeByPlayer()).isEmpty();

        gd.turnNumber = 1;
        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.UPKEEP_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Green anchor");
        harness.handleListChoice(player2, "Red waterfall");
        assertThat(source.getChosenModeByPlayer()).containsEntry(player1.getId(), "Green anchor")
                .containsEntry(player2.getId(), "Red waterfall");
    }

    private PlanechaseService setupPlane() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        PlanarObject source = new PlanarObject(new TwoStreamsFacility(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return GameTestEngineContext.get().getBean(PlanechaseService.class);
    }
}
