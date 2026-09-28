package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MetathranZombie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuelingGrounds.class, MetathranZombie.class})
class DuelingGroundsTest extends BaseCardTest {

    @Test
    @DisplayName("No more than one creature can attack each combat")
    void limitsAttackers() {
        harness.addToBattlefield(player2, new DuelingGrounds());
        addCreatureReady(player1, new MetathranZombie());
        addCreatureReady(player1, new MetathranZombie());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack");
    }

    @Test
    @DisplayName("One attacker is legal")
    void allowsOneAttacker() {
        harness.addToBattlefield(player2, new DuelingGrounds());
        addCreatureReady(player1, new MetathranZombie());

        assertThatCode(() -> declareAttackers(player1, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No more than one distinct creature can block each combat")
    void limitsBlockers() {
        harness.addToBattlefield(player1, new DuelingGrounds());
        addReadyAttacker(player1);
        addCreatureReady(player2, new MetathranZombie());
        addCreatureReady(player2, new MetathranZombie());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 distinct creature can block each combat");
    }

    @Test
    @DisplayName("One blocker is legal")
    void allowsOneBlocker() {
        harness.addToBattlefield(player1, new DuelingGrounds());
        addReadyAttacker(player1);
        addCreatureReady(player2, new MetathranZombie());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1)))).doesNotThrowAnyException();
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new MetathranZombie());
        attacker.setAttacking(true);
        return attacker;
    }
}
