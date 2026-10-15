package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Naya.class, Forest.class, GrizzlyBears.class, HillGiant.class, WalkingCorpse.class,
        YouthfulKnight.class})
class NayaTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Naya(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void controllerMayPlayAnyNumberOfLands() {
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void chaosBoostsEligibleCreatureByNumberOfLandsControllerControls() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent ineligible = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new HillGiant());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId()).doesNotContain(ineligible.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void nextActivePlayerAlsoMayPlayAnyNumberOfLands() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player2, 0);
        harness.playLand(player2, 0);
        harness.playLand(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
    }

    @Test
    void additionalLandPermissionEndsWhenNayaLeaves() {
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        gd.planechase.faceUp.clear();

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void chaosCanTargetRedAndWhiteCreatures() {
        harness.addToBattlefield(player1, new Forest());
        Permanent red = addCreatureReady(player1, new HillGiant());
        Permanent white = addCreatureReady(player1, new YouthfulKnight());

        beginChaosTargetChoice();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(red.getId(), white.getId());
        harness.handlePermanentChosen(player1, white.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(3);
    }

    @Test
    void chaosCountsLandsAtResolutionAndBoostDoesNotTrackLaterChanges() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        beginChaosTargetChoice();
        harness.handlePermanentChosen(player1, target.getId());
        harness.addToBattlefield(player1, new Forest());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void chaosWithNoControlledLandsGivesNoBoost() {
        harness.addToBattlefield(player2, new Forest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        beginChaosTargetChoice();
        harness.handlePermanentChosen(player1, target.getId());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void chaosDoesNotBoostCreatureThatOpponentGainsBeforeResolution() {
        harness.addToBattlefield(player1, new Forest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        beginChaosTargetChoice();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chaosResolvesAfterNayaLeavesAndBoostExpiresAtEndOfTurn() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new Forest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        beginChaosTargetChoice();
        harness.handlePermanentChosen(player1, target.getId());
        gd.planechase.faceUp.clear();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void chaosWithoutEligibleCreatureDoesNotAskForTarget() {
        addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new GrizzlyBears());

        beginChaosTargetChoice();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void beginChaosTargetChoice() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));
    }
}
