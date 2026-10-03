package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.m.MourningThrull;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaggerclawImp.class, Gristleback.class, MourningThrull.class})
class DaggerclawImpTest extends BaseCardTest {

    @Test
    @DisplayName("Daggerclaw Imp cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new DaggerclawImp());
        Permanent attacker = addCreatureReady(player1, new Gristleback());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Daggerclaw Imp cannot be blocked by a creature without flying")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new DaggerclawImp());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Gristleback());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Daggerclaw Imp (flying)");
    }

    @Test
    @DisplayName("Daggerclaw Imp can attack and be blocked by a flying creature")
    void canAttackAndBeBlockedByFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new DaggerclawImp());
        Permanent blocker = addCreatureReady(player2, new MourningThrull());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactly(0);
    }

    @Test
    @DisplayName("Daggerclaw Imp cannot block a flying attacker either")
    void cannotBlockFlyingAttacker() {
        addCreatureReady(player2, new DaggerclawImp());
        Permanent attacker = addCreatureReady(player1, new MourningThrull());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
