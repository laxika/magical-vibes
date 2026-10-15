package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hipparion.class, BalduvianBarbarians.class, BalduvianBears.class})
class HipparionTest extends BaseCardTest {

    private Permanent setupBlock(Permanent attacker) {
        Permanent hipparion = addCreatureReady(player2, new Hipparion());

        attacker.setAttacking(true);

        prepareDeclareBlockers();
        return hipparion;
    }

    @Test
    @DisplayName("Blocking a power-3+ creature requires paying {1}, which is charged from the pool")
    void payingLetsItBlockHighPower() {
        Permanent giant = addCreatureReady(player1, new BalduvianBarbarians());
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
        Permanent giant = addCreatureReady(player1, new BalduvianBarbarians());
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
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent hipparion = setupBlock(bears);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(hipparion);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(hipparion.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An attacker raised from power 2 to power 3 requires payment")
    void increasedPowerRequiresPayment() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent hipparion = setupBlock(bears);
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(hipparion);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("block cost");
        assertThat(hipparion.isBlocking()).isFalse();

        harness.addMana(player2, ManaColor.GREEN, 1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(hipparion.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An attacker reduced from power 3 to power 2 can be blocked for free")
    void reducedPowerDoesNotRequirePayment() {
        Permanent barbarians = addCreatureReady(player1, new BalduvianBarbarians());
        barbarians.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent hipparion = setupBlock(barbarians);
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(hipparion);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(barbarians);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(hipparion.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Blocking a low-power attacker leaves available mana unspent")
    void freeBlockDoesNotSpendAvailableMana() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent hipparion = setupBlock(bears);
        harness.addMana(player2, ManaColor.RED, 2);
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(hipparion);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(hipparion.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }
}
