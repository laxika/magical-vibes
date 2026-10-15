package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GlacialWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({EvilEyeOfOrmsByGore.class, GlacialWall.class, GrizzlyBears.class, Unsummon.class})
class EvilEyeOfOrmsByGoreTest extends BaseCardTest {
    @Test
    @DisplayName("A non-Eye creature you control cannot attack while Evil Eye is on the battlefield")
    void nonEyeCreatureCannotAttack() {
        harness.addToBattlefield(player1, new EvilEyeOfOrmsByGore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(bearsIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Evil Eye itself (an Eye) can still attack")
    void evilEyeCanAttack() {
        addCreatureReady(player1, new EvilEyeOfOrmsByGore());

        harness.setLife(player2, 20);
        declareAttackers(List.of(0));

        // Evil Eye is 3/6, unblocked
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An Eye creature you control can attack")
    void eyeCreatureCanAttack() {
        harness.addToBattlefield(player1, new EvilEyeOfOrmsByGore());
        harness.setLife(player2, 20);

        Permanent eyePerm = addCreatureReady(player1, new EvilEyeOfOrmsByGore());

        int eyeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(eyePerm);
        declareAttackers(List.of(eyeIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The opponent's non-Eye creatures are unaffected (restriction is controller-scoped)")
    void opponentNonEyeCreatureCanAttack() {
        harness.addToBattlefield(player1, new EvilEyeOfOrmsByGore());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        declareAttackers(player2, List.of(bearsIndex));

        assertThat(bears.isAttacking()).isTrue();
    }
    @Test
    @DisplayName("Evil Eye cannot be blocked by a non-Wall creature")
    void cannotBeBlockedByNonWall() {
        addCreatureReady(player1, new EvilEyeOfOrmsByGore());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Walls");
    }

    @Test
    @DisplayName("Evil Eye can be blocked by a Wall")
    void canBeBlockedByWall() {
        addCreatureReady(player1, new EvilEyeOfOrmsByGore());
        Permanent wall = addCreatureReady(player2, new GlacialWall());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Non-Eye creatures can attack after the last Evil Eye leaves the battlefield")
    void nonEyeCanAttackAfterEyeLeaves() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EvilEyeOfOrmsByGore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, eye.getId());

        harness.assertNotOnBattlefield(player1, "Evil Eye of Orms-by-Gore");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Removing one Evil Eye does not remove another Evil Eye's attack restriction")
    void anotherEyeKeepsAttackRestriction() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EvilEyeOfOrmsByGore());
        harness.addToBattlefield(player1, new EvilEyeOfOrmsByGore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, eye.getId());

        assertThat(countPermanents(player1, "Evil Eye of Orms-by-Gore")).isEqualTo(1);
        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        assertThatThrownBy(() -> declareAttackers(List.of(bearsIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Eye creatures can still block while their controller controls Evil Eye")
    void nonEyeCanStillBlock() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new EvilEyeOfOrmsByGore());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Evil Eye can block a non-Wall attacker")
    void eyeCanBlockNonWall() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent eye = addCreatureReady(player2, new EvilEyeOfOrmsByGore());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(eye.isBlocking()).isTrue();
    }
}
