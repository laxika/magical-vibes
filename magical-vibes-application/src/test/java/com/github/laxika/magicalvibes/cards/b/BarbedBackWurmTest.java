package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarbedBackWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Green blocker gets -1/-1")
    void greenBlockerGetsMinusOneMinusOne() {
        addCreatureReady(player1, new BarbedBackWurm());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        blockWurmWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new BarbedBackWurm());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        blockWurmWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-green blocker is an illegal target")
    void nonGreenBlockerCannotBeTargeted() {
        addCreatureReady(player1, new BarbedBackWurm());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        blockWurmWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A green creature that isn't blocking the Wurm is an illegal target")
    void nonBlockingGreenCreatureCannotBeTargeted() {
        addCreatureReady(player1, new BarbedBackWurm());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        blockWurmWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Attacks with the Wurm and blocks it with player2's creature at {@code blockerIndex}. */
    private void blockWurmWith(int blockerIndex) {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, 0)));
        resolveAllTriggers();
    }
}
