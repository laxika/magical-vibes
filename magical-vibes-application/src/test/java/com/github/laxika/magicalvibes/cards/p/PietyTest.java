package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PietyTest extends BaseCardTest {

    @Test
    @DisplayName("Only blocking creatures get +0/+3")
    void buffsOnlyBlockingCreatures() {
        Permanent blocker = addBlockingCreature(player2);
        Permanent bystander = addReadyCreature(player1);
        Permanent attacker = addReadyCreature(player1);
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
        harness.setHand(player1, List.of(new Piety()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addBlockingCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        gd.playerBattlefields.get(player.getId()).add(blocker);
        return blocker;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
