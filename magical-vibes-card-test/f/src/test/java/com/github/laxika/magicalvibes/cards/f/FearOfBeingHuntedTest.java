package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalemurkLeech;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearOfBeingHunted.class, BalemurkLeech.class})
class FearOfBeingHuntedTest extends BaseCardTest {

    @Test
    @DisplayName("Must be blocked when an able blocker exists")
    void mustBeBlockedIfAble() {
        Permanent fear = addCreatureReady(player1, new FearOfBeingHunted());
        fear.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalemurkLeech());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unable blocker does not satisfy the requirement")
    void doesNotRequireBlockWhenNoAbleBlockerExists() {
        Permanent fear = addCreatureReady(player1, new FearOfBeingHunted());
        fear.setAttacking(true);
        Permanent tappedBlocker = addCreatureReady(player2, new BalemurkLeech());
        tappedBlocker.tap();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Haste permits attacking on the turn it enters")
    void canAttackWhileSummoningSick() {
        Permanent fear = harness.addToBattlefieldAndReturn(player1, new FearOfBeingHunted());
        fear.setSummoningSick(true);
        addCreatureReady(player2, new BalemurkLeech());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(fear.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Only one blocker is required even when two are available")
    void oneBlockerSatisfiesRequirement() {
        Permanent fear = addCreatureReady(player1, new FearOfBeingHunted());
        fear.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalemurkLeech());
        Permanent otherBlocker = addCreatureReady(player2, new BalemurkLeech());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(otherBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("One blocker may choose either of two creatures that must be blocked")
    void competingRequirementsWithOneBlocker() {
        Permanent first = addCreatureReady(player1, new FearOfBeingHunted());
        Permanent second = addCreatureReady(player1, new FearOfBeingHunted());
        first.setAttacking(true);
        second.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalemurkLeech());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Two available blockers must satisfy both attacking creatures' requirements")
    void cannotDoubleBlockOneFearAndLeaveAnotherUnblocked() {
        Permanent first = addCreatureReady(player1, new FearOfBeingHunted());
        Permanent second = addCreatureReady(player1, new FearOfBeingHunted());
        first.setAttacking(true);
        second.setAttacking(true);
        addCreatureReady(player2, new BalemurkLeech());
        addCreatureReady(player2, new BalemurkLeech());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
    }

    @Test
    @DisplayName("Blocking an ordinary attacker cannot bypass the requirement")
    void cannotChooseOrdinaryAttackerInstead() {
        Permanent fear = addCreatureReady(player1, new FearOfBeingHunted());
        Permanent otherAttacker = addCreatureReady(player1, new BalemurkLeech());
        fear.setAttacking(true);
        otherAttacker.setAttacking(true);
        addCreatureReady(player2, new BalemurkLeech());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");
    }
}
