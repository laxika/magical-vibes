package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AplanMortarium.class, GrizzlyBears.class})
class AplanMortariumTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new AplanMortarium(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    void upkeepAddsExposureCounterAndControllerLosesLifeEqualToTotal() {
        harness.setLife(player1, 20);
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);

        triggerUpkeep(steps);
        assertThat(source.getCounters()).containsEntry(CounterType.EXPOSURE, 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);

        triggerUpkeep(steps);
        assertThat(source.getCounters()).containsEntry(CounterType.EXPOSURE, 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void chaosCreatesTwoAlienAngelTokensWithTheSpellCastAbility() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Alien Angel");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.isCreature(gd, token)).isTrue();
            assertThat(gqs.isArtifact(gd, token)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        });

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.isCreature(gd, token)).isFalse();
            assertThat(gqs.isArtifact(gd, token)).isTrue();
        });
    }

    private void triggerUpkeep(StepTriggerService steps) {
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
        harness.passBothPriorities();
    }
}
