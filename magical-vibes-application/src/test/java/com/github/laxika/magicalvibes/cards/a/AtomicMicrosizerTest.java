package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtomicMicrosizer.class, GrizzlyBears.class})
class AtomicMicrosizerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent microsizer = addMicrosizerReady();
        microsizer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking lets the controller make a target creature 1/1 and unblockable")
    void attackTriggerMakesTargetOneOneAndUnblockable() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent microsizer = addMicrosizerReady();
        microsizer.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger can be declined and its effects expire at end of turn")
    void attackTriggerCanBeDeclinedAndExpires() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent microsizer = addMicrosizerReady();
        microsizer.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, target)).isFalse();

        declareEndStep();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private Permanent addMicrosizerReady() {
        Permanent microsizer = harness.addToBattlefieldAndReturn(player1, new AtomicMicrosizer());
        microsizer.setSummoningSick(false);
        return microsizer;
    }

    private void declareEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
