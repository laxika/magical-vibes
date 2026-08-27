package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VertigoSpawn.class, GrizzlyBears.class})
class VertigoSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature taps it and prevents its next untap")
    void blockingTapsAndLocksAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent spawn = addCreatureReady(player2, new VertigoSpawn());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Vertigo Spawn")
                        && entry.getTargetId().equals(attacker.getId())
                        && entry.isNonTargeting()
                        && entry.getSourcePermanentId().equals(spawn.getId()));

        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Vertigo Spawn does not trigger when it does not block")
    void doesNotTriggerWithoutBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new VertigoSpawn());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.getSkipUntapCount()).isZero();
    }
}
