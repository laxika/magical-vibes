package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EscapedNullTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives Escaped Null +5/+0 until end of turn")
    void blockingGivesPlusFivePlusZero() {
        Permanent escapedNull = addReadyNull(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(escapedNull.getPowerModifier()).isEqualTo(5);
        assertThat(escapedNull.getToughnessModifier()).isZero();
        assertThat(escapedNull.getEffectivePower()).isEqualTo(6);
        assertThat(escapedNull.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Becoming blocked gives Escaped Null +5/+0 until end of turn")
    void becomingBlockedGivesPlusFivePlusZero() {
        Permanent escapedNull = addReadyNull(player1);
        escapedNull.setAttacking(true);
        addReadyAttacker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(escapedNull.getPowerModifier()).isEqualTo(5);
        assertThat(escapedNull.getToughnessModifier()).isZero();
        assertThat(escapedNull.getEffectivePower()).isEqualTo(6);
        assertThat(escapedNull.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Becoming blocked triggers only once with multiple blockers")
    void multipleBlockersGiveOnlyOneBoost() {
        Permanent escapedNull = addReadyNull(player1);
        escapedNull.setAttacking(true);
        addReadyAttacker(player2);
        addReadyAttacker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(escapedNull.getPowerModifier()).isEqualTo(5);
        assertThat(escapedNull.getEffectivePower()).isEqualTo(6);
    }

    @Test
    @DisplayName("The temporary boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent escapedNull = addReadyNull(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(escapedNull.getPowerModifier()).isZero();
        assertThat(escapedNull.getToughnessModifier()).isZero();
        assertThat(escapedNull.getEffectivePower()).isEqualTo(1);
        assertThat(escapedNull.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyNull(Player player) {
        Permanent permanent = new Permanent(new EscapedNull());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyAttacker(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
