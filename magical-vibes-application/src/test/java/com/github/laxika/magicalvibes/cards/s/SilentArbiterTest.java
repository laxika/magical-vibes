package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
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

@CardUsed({SilentArbiter.class, DrossCrocodile.class})
class SilentArbiterTest extends BaseCardTest {

    @Test
    @DisplayName("No more than one creature can attack each combat")
    void limitsAttackers() {
        addCreatureReady(player2, new SilentArbiter());
        addCreatureReady(player1, new DrossCrocodile());
        addCreatureReady(player1, new DrossCrocodile());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack");
    }

    @Test
    @DisplayName("One attacker is legal")
    void allowsOneAttacker() {
        addCreatureReady(player2, new SilentArbiter());
        addCreatureReady(player1, new DrossCrocodile());

        assertThatCode(() -> declareAttackers(player1, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No more than one distinct creature can block each combat")
    void limitsBlockers() {
        addCreatureReady(player1, new SilentArbiter());
        addReadyAttacker(player1);
        addCreatureReady(player2, new DrossCrocodile());
        addCreatureReady(player2, new DrossCrocodile());
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
        addCreatureReady(player1, new SilentArbiter());
        addReadyAttacker(player1);
        addCreatureReady(player2, new DrossCrocodile());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1)))).doesNotThrowAnyException();
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new DrossCrocodile());
        attacker.setAttacking(true);
        return attacker;
    }
}
