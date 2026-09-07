package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.w.WildElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbedBackWurm.class, IronTuskElephant.class, WildElephant.class})
class BarbedBackWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Green blocker gets -1/-1")
    void greenBlockerGetsMinusOneMinusOne() {
        addCreatureReady(player1, new BarbedBackWurm());
        Permanent blocker = addCreatureReady(player2, new WildElephant());

        blockWurmWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new BarbedBackWurm());
        Permanent blocker = addCreatureReady(player2, new WildElephant());

        blockWurmWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-green blocker is an illegal target")
    void nonGreenBlockerCannotBeTargeted() {
        addCreatureReady(player1, new BarbedBackWurm());
        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());

        blockWurmWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A green creature that isn't blocking the Wurm is an illegal target")
    void nonBlockingGreenCreatureCannotBeTargeted() {
        addCreatureReady(player1, new BarbedBackWurm());
        addCreatureReady(player2, new WildElephant());
        Permanent bystander = addCreatureReady(player2, new WildElephant());

        blockWurmWith(0);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A green creature blocking another attacker is an illegal target")
    void blockerOfAnotherAttackerCannotBeTargeted() {
        addCreatureReady(player1, new BarbedBackWurm());
        addCreatureReady(player1, new BarbedBackWurm());
        Permanent blocker = addCreatureReady(player2, new WildElephant());

        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Black mana is required to activate the ability")
    void blackManaIsRequired() {
        addCreatureReady(player1, new BarbedBackWurm());
        Permanent blocker = addCreatureReady(player2, new WildElephant());

        blockWurmWith(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
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
