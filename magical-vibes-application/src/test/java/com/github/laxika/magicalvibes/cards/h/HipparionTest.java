package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class HipparionTest extends BaseCardTest {

    private Permanent setupBlock(Permanent attacker) {
        Permanent hipparion = new Permanent(new Hipparion());
        hipparion.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(hipparion);

        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        return hipparion;
    }

    @Test
    @DisplayName("Blocking a power-3+ creature requires paying {1}, which is charged from the pool")
    void payingLetsItBlockHighPower() {
        Permanent giant = new Permanent(new HillGiant());
        Permanent hipparion = setupBlock(giant);
        harness.addMana(player2, ManaColor.WHITE, 1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(hipparion);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(giant);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(hipparion.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot block a power-3+ creature without the mana to pay {1}")
    void cannotBlockHighPowerWithoutMana() {
        Permanent giant = new Permanent(new HillGiant());
        Permanent hipparion = setupBlock(giant);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(hipparion);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(giant);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("block cost");
        assertThat(hipparion.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Blocking a creature with power less than 3 is free")
    void blockingLowPowerIsFree() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent hipparion = setupBlock(bears);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(hipparion);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(hipparion.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
