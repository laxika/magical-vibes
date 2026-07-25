package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.e.EyeForAnEye;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameServiceMutationCoordinatorTest extends BaseCardTest {

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
        Permanent source = addCreatureReady(player2, new GoblinPiker());

        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();
        long parkedActionVersion = gd.domainStateVersion();

        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
        assertThat(gd.domainStateVersion()).isEqualTo(parkedActionVersion + 1);
    }
}
