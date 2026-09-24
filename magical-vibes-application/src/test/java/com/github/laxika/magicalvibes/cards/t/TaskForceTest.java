package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Buoyancy;
import com.github.laxika.magicalvibes.cards.s.StingingBarrier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaskForce.class, Buoyancy.class, StingingBarrier.class})
class TaskForceTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+3 until end of turn when it becomes the target of a spell")
    void gainsBoostWhenTargetedBySpell() {
        Permanent taskForce = harness.addToBattlefieldAndReturn(player1, new TaskForce());
        harness.setHand(player2, List.of(new Buoyancy()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player2, 0, taskForce.getId());
        harness.passBothPriorities();

        assertThat(taskForce.getToughnessModifier()).isEqualTo(3);
        assertThat(taskForce.getEffectivePower()).isEqualTo(1);
        assertThat(taskForce.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Gets +0/+3 for each time it becomes a target in the same turn")
    void gainsBoostEachTimeTargeted() {
        Permanent taskForce = harness.addToBattlefieldAndReturn(player1, new TaskForce());
        harness.setHand(player2, List.of(new Buoyancy(), new Buoyancy()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castEnchantment(player2, 0, taskForce.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player2, 0, taskForce.getId());
        harness.passBothPriorities();

        assertThat(taskForce.getToughnessModifier()).isEqualTo(6);
        assertThat(taskForce.getEffectivePower()).isEqualTo(1);
        assertThat(taskForce.getEffectiveToughness()).isEqualTo(9);
    }

    @Test
    @DisplayName("Gets +0/+3 until end of turn when it becomes the target of an ability")
    void gainsBoostWhenTargetedByAbility() {
        Permanent taskForce = addCreatureReady(player1, new TaskForce());
        addCreatureReady(player2, new StingingBarrier());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, taskForce.getId());
        harness.passBothPriorities();

        assertThat(taskForce.getToughnessModifier()).isEqualTo(3);
        assertThat(taskForce.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Gets +0/+3 when targeted by an ability it controls")
    void gainsBoostWhenTargetedByOwnAbility() {
        Permanent taskForce = addCreatureReady(player1, new TaskForce());
        addCreatureReady(player1, new StingingBarrier());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, taskForce.getId());
        harness.passBothPriorities();

        assertThat(taskForce.getToughnessModifier()).isEqualTo(3);
        assertThat(taskForce.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent taskForce = addCreatureReady(player1, new TaskForce());
        addCreatureReady(player2, new StingingBarrier());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, taskForce.getId());
        harness.passBothPriorities();

        assertThat(taskForce.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(taskForce.getToughnessModifier()).isEqualTo(0);
        assertThat(taskForce.getEffectiveToughness()).isEqualTo(3);
    }
}
