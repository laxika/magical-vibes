package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilkenfistOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked untaps Silkenfist Order")
    void becomingBlockedUntapsIt() {
        Permanent order = addCreatureReady(player1, new SilkenfistOrder());
        order.setAttacking(true);
        order.tap();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(order.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(order.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple blockers trigger Silkenfist Order only once")
    void multipleBlockersTriggerOnce() {
        Permanent order = addCreatureReady(player1, new SilkenfistOrder());
        order.setAttacking(true);
        order.tap();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(order.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An unblocked Silkenfist Order does not trigger")
    void unblockedDoesNotTrigger() {
        Permanent order = addCreatureReady(player1, new SilkenfistOrder());
        order.setAttacking(true);
        order.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(order.isTapped()).isTrue();
    }
}
