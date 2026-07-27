package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfernoElementalTest extends BaseCardTest {

    // ===== When Inferno Elemental blocks =====

    @Test
    @DisplayName("Blocking creates a trigger that deals 3 damage to the attacker")
    void blockingDeals3DamageToAttacker() {
        Permanent elemental = addReadyElemental(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Inferno Elemental");
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());

        harness.passBothPriorities();

        // Attacker (2/2) takes 3 damage and dies
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== When Inferno Elemental becomes blocked =====

    @Test
    @DisplayName("Becoming blocked creates a trigger that deals 3 damage to the blocker")
    void becomingBlockedDeals3DamageToBlocker() {
        Permanent elemental = addReadyElemental(player1);
        elemental.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(elemental.getId());

        harness.passBothPriorities();

        // Blocker (2/2) takes 3 damage and dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates one trigger per blocker")
    void becomingBlockedByMultipleCreaturesCreatesMultipleTriggers() {
        Permanent elemental = addReadyElemental(player1);
        elemental.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Inferno Elemental"))
                .count();
        assertThat(triggerCount).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        // Both blockers (2/2) take 3 damage and die
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    // ===== Trigger is non-targeting =====

    @Test
    @DisplayName("Block trigger is non-targeting (cannot be fizzled by shroud/hexproof)")
    void blockTriggerIsNonTargeting() {
        Permanent elemental = addReadyElemental(player1);
        elemental.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.isNonTargeting()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyElemental(Player player) {
        Permanent perm = new Permanent(new InfernoElemental());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
