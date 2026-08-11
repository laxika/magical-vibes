package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeepchannelDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk you control get +1/+1")
    void buffsOtherMerfolkYouControl() {
        addDuelist(player1);
        Permanent merfolk = addMerfolk(player1);

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff itself, non-Merfolk creatures, or an opponent's Merfolk")
    void limitsStaticBoost() {
        Permanent duelist = addDuelist(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentMerfolk = addMerfolk(player2);

        assertThat(gqs.getEffectivePower(gd, duelist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duelist)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentMerfolk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps a target Merfolk you control at your end step")
    void untapsTargetControlledMerfolkAtEndStep() {
        addDuelist(player1);
        Permanent merfolk = addMerfolk(player1);
        UUID merfolkId = merfolk.getId();
        merfolk.tap();

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, merfolkId);
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("End-step trigger cannot target an opponent's or non-Merfolk creature")
    void restrictsEndStepTarget() {
        addDuelist(player1);
        Permanent opponentMerfolk = addMerfolk(player2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(opponentMerfolk.getId(), bears.getId());
    }

    @Test
    @DisplayName("Does not trigger on an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        addDuelist(player1);
        Permanent merfolk = addMerfolk(player1);
        merfolk.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(merfolk.isTapped()).isTrue();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addDuelist(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DeepchannelDuelist());
    }

    private Permanent addMerfolk(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CoralMerfolk());
    }
}
