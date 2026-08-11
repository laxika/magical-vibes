package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TributaryVaulterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Merfolk you control lets another Merfolk get +2/+0")
    void tappingControlledMerfolkBoostsAnotherMerfolk() {
        Permanent vaulter = addCreatureReady(player1, new TributaryVaulter());
        Permanent targetMerfolk = addCreatureReady(player1, new CoralMerfolk());

        tap(vaulter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        harness.passBothPriorities();

        assertThat(targetMerfolk.getPowerModifier()).isEqualTo(2);
        assertThat(targetMerfolk.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The trigger can target only another Merfolk you control")
    void triggerRestrictsTargets() {
        Permanent vaulter = addCreatureReady(player1, new TributaryVaulter());
        Permanent otherMerfolk = addCreatureReady(player1, new CoralMerfolk());
        Permanent targetMerfolk = addCreatureReady(player1, new CoralMerfolk());
        Permanent nonMerfolk = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentMerfolk = addCreatureReady(player2, new CoralMerfolk());

        tap(vaulter);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetMerfolk.getId())
                .contains(otherMerfolk.getId())
                .doesNotContain(vaulter.getId(), nonMerfolk.getId(), opponentMerfolk.getId());

        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent vaulter = addCreatureReady(player1, new TributaryVaulter());
        Permanent targetMerfolk = addCreatureReady(player1, new CoralMerfolk());

        tap(vaulter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(targetMerfolk.getPowerModifier()).isZero();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });
    }
}
