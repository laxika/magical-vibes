package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernoElemental.class, RuneclawBear.class})
class InfernoElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking creates a trigger that deals 3 damage to the attacker")
    void blockingDeals3DamageToAttacker() {
        addCreatureReady(player2, new InfernoElemental());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
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
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Becoming blocked creates a trigger that deals 3 damage to the blocker")
    void becomingBlockedDeals3DamageToBlocker() {
        Permanent elemental = addCreatureReady(player1, new InfernoElemental());
        elemental.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(elemental.getId());

        harness.passBothPriorities();

        // Blocker (2/2) takes 3 damage and dies
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates one trigger per blocker")
    void becomingBlockedByMultipleCreaturesCreatesMultipleTriggers() {
        Permanent elemental = addCreatureReady(player1, new InfernoElemental());
        elemental.setAttacking(true);
        addCreatureReady(player2, new RuneclawBear());
        addCreatureReady(player2, new RuneclawBear());

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
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Runeclaw Bear"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Both Elementals deal exactly 3 damage before combat damage")
    void opposingElementalsBothTrigger() {
        Permanent attacker = addCreatureReady(player1, new InfernoElemental());
        Permanent blocker = addCreatureReady(player2, new InfernoElemental());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Inferno Elemental");
        harness.assertOnBattlefield(player2, "Inferno Elemental");
    }

    @Test
    @DisplayName("Block trigger is non-targeting (cannot be fizzled by shroud/hexproof)")
    void blockTriggerIsNonTargeting() {
        Permanent elemental = addCreatureReady(player1, new InfernoElemental());
        elemental.setAttacking(true);
        addCreatureReady(player2, new RuneclawBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.isNonTargeting()).isTrue();
    }

}
