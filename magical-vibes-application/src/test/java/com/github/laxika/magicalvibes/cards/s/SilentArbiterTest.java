package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.Permanent;
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
        addCreatureReady(player1, new DrossCrocodile());
        addCreatureReady(player2, new DrossCrocodile());
        addCreatureReady(player2, new DrossCrocodile());
        declareAttackersAndPrepareBlockers(List.of(1));

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
        addCreatureReady(player1, new DrossCrocodile());
        addCreatureReady(player2, new DrossCrocodile());
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1)))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The Arbiter also limits its controller's attackers")
    void limitsItsControllersAttackers() {
        addCreatureReady(player1, new SilentArbiter());
        addCreatureReady(player1, new DrossCrocodile());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack");
    }

    @Test
    @DisplayName("A tapped Arbiter still limits blockers")
    void tappedArbiterStillLimitsBlockers() {
        Permanent arbiter = addCreatureReady(player1, new SilentArbiter());
        arbiter.setTapped(true);
        addCreatureReady(player1, new DrossCrocodile());
        addCreatureReady(player2, new DrossCrocodile());
        addCreatureReady(player2, new DrossCrocodile());
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 distinct creature can block each combat");
    }

    @Test
    @DisplayName("Removing the Arbiter before blockers are declared lifts the blocker limit")
    void removalBeforeBlockingLiftsBlockerLimit() {
        Permanent arbiter = addCreatureReady(player2, new SilentArbiter());
        addCreatureReady(player1, new DrossCrocodile());
        addCreatureReady(player2, new DrossCrocodile());
        addCreatureReady(player2, new DrossCrocodile());
        declareAttackersAndPrepareBlockers(List.of(0));
        gd.playerBattlefields.get(player2.getId()).remove(arbiter);
        gd.playerGraveyards.get(player2.getId()).add(arbiter.getCard());

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }
}
