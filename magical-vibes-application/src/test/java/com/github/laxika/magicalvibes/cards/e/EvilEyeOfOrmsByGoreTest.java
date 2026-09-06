package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GlacialWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvilEyeOfOrmsByGore.class, GlacialWall.class, GrizzlyBears.class})
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
        Permanent evilEye = addCreatureReady(player1, new EvilEyeOfOrmsByGore());
        evilEye.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Walls");
    }

    @Test
    @DisplayName("Evil Eye can be blocked by a Wall")
    void canBeBlockedByWall() {
        Permanent evilEye = addCreatureReady(player1, new EvilEyeOfOrmsByGore());
        evilEye.setAttacking(true);

        Permanent wall = addCreatureReady(player2, new GlacialWall());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }
}
