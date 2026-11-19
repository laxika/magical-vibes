package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.c.CanopyStalker;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.e.EkunduCyclops;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilentArbiter.class, DrossCrocodile.class, BerserkersOfBloodRidge.class, GrizzlyBears.class,
        CanopyStalker.class, EkunduCyclops.class, ViashinoWarrior.class})
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
        arbiter.tap();
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

    @Test
    @DisplayName("With two creatures that attack each combat if able, exactly one of them attacks")
    void exactlyOneOfTwoForcedAttackersAttacks() {
        addCreatureReady(player2, new SilentArbiter());
        addCreatureReady(player1, new BerserkersOfBloodRidge());
        addCreatureReady(player1, new BerserkersOfBloodRidge());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack");
        assertThatCode(() -> declareAttackers(player1, List.of(1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Either of two creatures that attack each combat if able may be the one that attacks")
    void eitherForcedAttackerMayAttack() {
        addCreatureReady(player2, new SilentArbiter());
        addCreatureReady(player1, new BerserkersOfBloodRidge());
        addCreatureReady(player1, new BerserkersOfBloodRidge());

        assertThatCode(() -> declareAttackers(player1, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Only the creature that attacks if able may attack, alone, beside a creature without abilities")
    void onlyTheForcedCreatureAttacks() {
        addCreatureReady(player2, new SilentArbiter());
        addCreatureReady(player1, new BerserkersOfBloodRidge());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThatCode(() -> declareAttackers(player1, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Only one of the creatures that attack if able is flagged as forced to attack")
    void onlyOneForcedAttackerIsFlagged() {
        addCreatureReady(player2, new SilentArbiter());
        addCreatureReady(player1, new BerserkersOfBloodRidge());
        addCreatureReady(player1, new BerserkersOfBloodRidge());
        harness.forceActivePlayer(player1);

        List<Integer> attackable = harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId());
        assertThat(harness.getCombatAttackService().getMustAttackIndices(gd, player1.getId(), attackable))
                .hasSize(1);
    }

    @Test
    @DisplayName("With two creatures that must be blocked if able attacking, blocking one of them is enough")
    void blockingOneOfTwoMustBeBlockedAttackersIsEnough() {
        addCreatureReady(player1, new CanopyStalker());
        addCreatureReady(player1, new CanopyStalker());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        addCreatureReady(player2, new SilentArbiter());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature that attacks if another creature attacks need not join an attacker it can't attack beside")
    void conditionalAttackerNeedNotJoinBeyondTheLimit() {
        addCreatureReady(player2, new SilentArbiter());
        addCreatureReady(player1, new EkunduCyclops());
        addCreatureReady(player1, new ViashinoWarrior());

        assertThatCode(() -> declareAttackers(player1, List.of(1))).doesNotThrowAnyException();
    }
}
