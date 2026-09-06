package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Piety.class, GrizzlyBears.class})
class PietyTest extends BaseCardTest {

    @Test
    @DisplayName("Only blocking creatures get +0/+3")
    void buffsOnlyBlockingCreatures() {
        Permanent blocker = addBlockingCreature(player2);
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        castPiety();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);

        assertThat(bystander.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The buff wears off at end of turn")
    void buffWearsOffAtEndOfTurn() {
        Permanent blocker = addBlockingCreature(player2);

        castPiety();

        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    private void castPiety() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Piety(), "{2}{W}");
        harness.passBothPriorities();
    }

    private Permanent addBlockingCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent blocker = addCreatureReady(player, new GrizzlyBears());
        blocker.setBlocking(true);
        return blocker;
    }
}
