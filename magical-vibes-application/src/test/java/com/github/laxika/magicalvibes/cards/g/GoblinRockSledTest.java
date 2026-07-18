package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinRockSledTest extends BaseCardTest {

    // ===== Attack restriction: defending player must control a Mountain =====

    @Test
    @DisplayName("Goblin Rock Sled can attack when defending player controls a Mountain")
    void canAttackWhenDefenderControlsMountain() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent sled = addReadySled(player1);

        declareAttackers(player1, List.of(0));

        assertThat(sled.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Goblin Rock Sled cannot attack when defending player controls no Mountain")
    void cannotAttackWithoutMountain() {
        addReadySled(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Doesn't untap if it attacked during your last turn =====

    @Test
    @DisplayName("Attacking pushes a triggered ability sourced from Goblin Rock Sled")
    void attackTriggerPushesOntoStack() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent sled = addReadySled(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(sled.getId());
    }

    @Test
    @DisplayName("Resolving the attack trigger makes Goblin Rock Sled skip its next untap step")
    void attackingSkipsNextUntap() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent sled = addReadySled(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(sled.getSkipUntapCount()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadySled(Player player) {
        Permanent perm = new Permanent(new GoblinRockSled());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
