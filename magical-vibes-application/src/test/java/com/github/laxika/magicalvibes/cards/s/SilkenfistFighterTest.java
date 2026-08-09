package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilkenfistFighterTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked untaps Silkenfist Fighter")
    void becomingBlockedUntapsIt() {
        Permanent fighter = addCreatureReady(player1, new SilkenfistFighter());
        fighter.setAttacking(true);
        fighter.tap();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(fighter.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(fighter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple blockers trigger Silkenfist Fighter only once")
    void multipleBlockersTriggerOnce() {
        Permanent fighter = addCreatureReady(player1, new SilkenfistFighter());
        fighter.setAttacking(true);
        fighter.tap();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(fighter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An unblocked Silkenfist Fighter does not trigger")
    void unblockedDoesNotTrigger() {
        Permanent fighter = addCreatureReady(player1, new SilkenfistFighter());
        fighter.setAttacking(true);
        fighter.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(fighter.isTapped()).isTrue();
    }
}
