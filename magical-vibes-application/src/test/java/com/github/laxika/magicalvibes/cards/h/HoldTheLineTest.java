package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HoldTheLineTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking creatures get +7/+7, but other creatures do not")
    void boostsBlockingCreaturesOnly() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());

        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        castHoldTheLine();

        assertThat(blocker.getEffectivePower()).isEqualTo(9);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(9);
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(bystander.getEffectivePower()).isEqualTo(2);
        assertThat(bystander.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The blocking-creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());

        castHoldTheLine();

        assertThat(blocker.getEffectivePower()).isEqualTo(9);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(9);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    private void castHoldTheLine() {
        harness.setHand(player1, List.of(new HoldTheLine()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
