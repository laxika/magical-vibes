package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoyalPegasus.class, GreenwoodSentinel.class})
class LoyalPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Loyal Pegasus can't attack alone")
    void cantAttackAlone() {
        Permanent pegasus = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        pegasus.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Loyal Pegasus can attack with another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);

        Permanent pegasus = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        pegasus.setSummoningSick(false);

        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        sentinel.setSummoningSick(false);

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Loyal Pegasus can't block alone")
    void cantBlockAlone() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent pegasus = harness.addToBattlefieldAndReturn(player2, new LoyalPegasus());
        pegasus.setSummoningSick(false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Loyal Pegasus can block with another creature")
    void canBlockWithAnother() {
        Permanent attacker1 = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        attacker1.setSummoningSick(false);
        attacker1.setAttacking(true);

        Permanent attacker2 = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        attacker2.setSummoningSick(false);
        attacker2.setAttacking(true);

        Permanent pegasus = harness.addToBattlefieldAndReturn(player2, new LoyalPegasus());
        pegasus.setSummoningSick(false);

        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        sentinel.setSummoningSick(false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(pegasus.isBlocking()).isTrue();
        assertThat(sentinel.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature that stays home does not let Loyal Pegasus attack alone")
    void idleCreatureDoesNotPermitAttack() {
        Permanent pegasus = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        pegasus.setSummoningSick(false);
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        sentinel.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Loyal Pegasi can attack together")
    void twoPegasiCanAttackTogether() {
        harness.setLife(player2, 20);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        first.setSummoningSick(false);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        second.setSummoningSick(false);

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An idle blocker does not let Loyal Pegasus block alone")
    void idleCreatureDoesNotPermitBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new LoyalPegasus());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Loyal Pegasi can block the same attacker together")
    void twoPegasiCanBlockSameAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        attacker.setAttacking(true);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new LoyalPegasus());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LoyalPegasus());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Loyal Pegasus cannot be blocked by a creature without flying or reach")
    void groundCreatureCannotBlockPegasus() {
        Permanent pegasus = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        pegasus.setSummoningSick(false);
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        companion.setSummoningSick(false);
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
