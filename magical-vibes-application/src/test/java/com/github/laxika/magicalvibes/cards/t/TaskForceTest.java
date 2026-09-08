package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskForceTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+3 until end of turn when it becomes the target of a spell")
    void gainsBoostWhenTargetedBySpell() {
        harness.addToBattlefield(player1, new TaskForce());
        Permanent taskForce = findPermanent(player1, "Task Force");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, taskForce.getId());
        harness.passBothPriorities();

        assertThat(taskForce.getToughnessModifier()).isEqualTo(3);
        assertThat(taskForce.getEffectivePower()).isEqualTo(1);
        assertThat(taskForce.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Gets +0/+3 until end of turn when it becomes the target of an ability")
    void gainsBoostWhenTargetedByAbility() {
        Permanent taskForce = new Permanent(new TaskForce());
        taskForce.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(taskForce);
        Permanent pyro = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyro.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, taskForce.getId());
        harness.passBothPriorities();

        assertThat(taskForce.getToughnessModifier()).isEqualTo(3);
        assertThat(taskForce.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent taskForce = new Permanent(new TaskForce());
        taskForce.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(taskForce);
        Permanent pyro = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyro.setSummoningSick(false);

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
