package com.github.laxika.magicalvibes.cards.j;

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

@CardUsed({JungleLion.class, GrizzlyBears.class})
class JungleLionTest extends BaseCardTest {

    @Test
    @DisplayName("Jungle Lion cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new JungleLion());

        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Jungle Lion can still be declared as an attacker")
    void canBeDeclaredAsAttacker() {
        Permanent lion = addCreatureReady(player1, new JungleLion());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(lion.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Jungle Lion does not prevent another creature from blocking")
    void otherCreatureCanBlock() {
        Permanent lion = addCreatureReady(player1, new JungleLion());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(lion.isBlocking()).isFalse();
        assertThat(bears.isBlocking()).isTrue();
    }
}
