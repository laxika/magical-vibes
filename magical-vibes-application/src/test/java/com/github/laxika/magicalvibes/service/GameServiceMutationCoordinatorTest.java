package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.e.EyeForAnEye;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseLifeAtNextDrawStepUnlessPays;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeForAnEye.class, GrizzlyBears.class})
class GameServiceMutationCoordinatorTest extends BaseCardTest {

    @Test
    void drawStepPaymentCompletesInsideOneOuterAction() {
        GrizzlyBears source = new GrizzlyBears();
        gd.queueDelayedAction(new LoseLifeAtNextDrawStepUnlessPays(player1.getId(), 1, 1, source));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        long versionBeforeAction = gd.domainStateVersion();

        gs.payDrawStepLifeLoss(gd, player1, source.getId());

        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
        assertThat(gd.domainStateVersion()).isEqualTo(versionBeforeAction + 1);
    }

    @Test
    void nonactivePlayerRetainsPriorityToPayMultipleObligations() {
        GrizzlyBears source = new GrizzlyBears();
        LoseLifeAtNextDrawStepUnlessPays obligation =
                new LoseLifeAtNextDrawStepUnlessPays(player2.getId(), 1, 1, source);
        gd.queueDelayedAction(obligation);
        gd.queueDelayedAction(obligation);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        gs.payDrawStepLifeLoss(gd, player2, source.getId());
        assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player2.getId());
        gs.payDrawStepLifeLoss(gd, player2, source.getId());

        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void drawStepPaymentRequiresPriorityBeforeSpendingMana() {
        GrizzlyBears source = new GrizzlyBears();
        LoseLifeAtNextDrawStepUnlessPays obligation =
                new LoseLifeAtNextDrawStepUnlessPays(player2.getId(), 1, 1, source);
        gd.queueDelayedAction(obligation);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> gs.payDrawStepLifeLoss(gd, player2, source.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("priority");

        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).containsExactly(obligation);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void autoPassStepRecursionCompletesInsideOneOuterAction() {
        gd.aiPlayerIds.clear();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        long versionBeforeAction = gd.domainStateVersion();

        gs.passPriority(gd, player1);

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.domainStateVersion()).isEqualTo(versionBeforeAction + 1);
    }

    @Test
    void parkedResolutionResumesInASeparateInteractionAnswerAction() {
        harness.setHand(player1, List.of(new EyeForAnEye()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);
        Permanent source = addCreatureReady(player2, new GrizzlyBears());

        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();
        long parkedActionVersion = gd.domainStateVersion();

        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
        assertThat(gd.domainStateVersion()).isEqualTo(parkedActionVersion + 1);
    }
}
