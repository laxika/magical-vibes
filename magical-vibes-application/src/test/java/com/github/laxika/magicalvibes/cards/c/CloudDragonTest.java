package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CloudDragon.class, GrizzlyBears.class})
class CloudDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Cloud Dragon can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent dragonPerm = addCreatureReady(player2, new CloudDragon());

        addCreatureReady(player1, new CloudDragon());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(dragonPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cloud Dragon cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new CloudDragon());

        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Cloud Dragon")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new CloudDragon());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Unblocked Cloud Dragon deals 5 damage to defending player")
    void dealsFiveDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new CloudDragon());
        atkPerm.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
