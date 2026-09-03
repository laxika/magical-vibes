package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.DazzlingBeauty;
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

@CardUsed({JohtullWurm.class, BalduvianBears.class})
class JohtullWurmTest extends BaseCardTest {

    @Test
    @DisplayName("With a single blocker the wurm is unaffected")
    void oneBlockerNoPenalty() {
        Permanent wurm = addCreatureReady(player1, new JohtullWurm());
        wurm.setAttacking(true);
        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers the wurm gets -2/-1 until end of turn")
    void twoBlockersMinusTwoMinusOne() {
        Permanent wurm = addCreatureReady(player1, new JohtullWurm());
        wurm.setAttacking(true);
        addCreatureReady(player2, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isEqualTo(-2);
        assertThat(wurm.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("With three blockers the penalty scales to -4/-2")
    void threeBlockersMinusFourMinusTwo() {
        Permanent wurm = addCreatureReady(player1, new JohtullWurm());
        wurm.setAttacking(true);
        addCreatureReady(player2, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(wurm.getPowerModifier()).isEqualTo(-4);
        assertThat(wurm.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("If unblocked no penalty is applied")
    void unblockedNoPenalty() {
        Permanent wurm = addCreatureReady(player1, new JohtullWurm());
        wurm.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }

    @Test
    @CardUsed(DazzlingBeauty.class)
    @DisplayName("Becoming blocked without a blocker does not apply a penalty")
    void noPenaltyWhenBlockedWithoutBlockers() {
        Permanent wurm = addCreatureReady(player1, new JohtullWurm());
        addCreatureReady(player2, new BalduvianBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DazzlingBeauty()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, wurm.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(wurm.isBlockedWithoutBlockers()).isTrue();
        assertThat(wurm.getPowerModifier()).isZero();
        assertThat(wurm.getToughnessModifier()).isZero();
    }

}
