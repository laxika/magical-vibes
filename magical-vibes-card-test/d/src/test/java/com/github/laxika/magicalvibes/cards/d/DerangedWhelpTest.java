package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DerangedWhelp.class})
class DerangedWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Deranged Whelp cannot be blocked by only one creature")
    void cannotBeBlockedByOneCreature() {
        Permanent blocker = addReadyBlocker();
        Permanent attacker = addReadyAttacker();
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more");
    }

    @Test
    @DisplayName("Deranged Whelp can be blocked by two creatures")
    void canBeBlockedByTwoCreatures() {
        Permanent firstBlocker = addReadyBlocker();
        Permanent secondBlocker = addReadyBlocker();
        Permanent attacker = addReadyAttacker();
        prepareDeclareBlockers();

        int firstBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker);
        int secondBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(firstBlockerIndex, attackerIndex),
                new BlockerAssignment(secondBlockerIndex, attackerIndex)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Deranged Whelp can remain unblocked even when a blocker is available")
    void canRemainUnblocked() {
        Permanent blocker = addReadyBlocker();
        addReadyAttacker();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Deranged Whelp can be blocked by more than two creatures")
    void canBeBlockedByThreeCreatures() {
        Permanent firstBlocker = addReadyBlocker();
        Permanent secondBlocker = addReadyBlocker();
        Permanent thirdBlocker = addReadyBlocker();
        Permanent attacker = addReadyAttacker();
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, attackerIndex),
                new BlockerAssignment(1, attackerIndex),
                new BlockerAssignment(2, attackerIndex)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(thirdBlocker.isBlocking()).isTrue();
    }

    private Permanent addReadyBlocker() {
        return addCreatureReady(player2, new DerangedWhelp());
    }

    private Permanent addReadyAttacker() {
        Permanent attacker = addCreatureReady(player1, new DerangedWhelp());
        attacker.setAttacking(true);
        return attacker;
    }
}
