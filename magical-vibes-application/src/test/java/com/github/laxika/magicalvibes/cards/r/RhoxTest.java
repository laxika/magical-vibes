package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.v.VolrathTheFallen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rhox.class, Mossdog.class, VolrathTheFallen.class})
class RhoxTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Rhox assigns combat damage to defending player")
    void blockedRhoxAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent rhox = addCreatureReady(player1, new Rhox());
        Permanent blocker = addCreatureReady(player2, new Mossdog());
        declareBlockers(rhox, blocker);

        resolveCombat();

        // Rhox has "assign as though unblocked" — assign all damage to defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 5));

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player2, "Mossdog");
    }

    @Test
    @DisplayName("Blocked Rhox can assign combat damage to its blocker instead")
    void blockedRhoxAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent rhox = addCreatureReady(player1, new Rhox());
        Permanent blocker = addCreatureReady(player2, new Mossdog());
        declareBlockers(rhox, blocker);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertNotOnBattlefield(player2, "Mossdog");
        harness.assertInGraveyard(player2, "Mossdog");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blocked Rhox can assign all combat damage to the defending player with multiple blockers")
    void blockedRhoxAssignsDamageToDefendingPlayerWithMultipleBlockers() {
        harness.setLife(player2, 20);
        Permanent rhox = addCreatureReady(player1, new Rhox());
        Permanent firstBlocker = addCreatureReady(player2, new Mossdog());
        Permanent secondBlocker = addCreatureReady(player2, new Mossdog());
        declareBlockers(rhox, firstBlocker, secondBlocker);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 5));

        harness.assertLife(player2, 15);
        assertThat(findPermanents(player2, "Mossdog")).hasSize(2);
    }

    @Test
    @DisplayName("Rhox regeneration ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent rhox = addCreatureReady(player1, new Rhox());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        assertThat(rhox.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration saves Rhox from lethal combat damage while still damaging player")
    void regenerationSavesFromLethalCombatDamage() {
        harness.setLife(player2, 20);
        Permanent rhox = addCreatureReady(player1, new Rhox());
        Permanent blocker = addCreatureReady(player2, new VolrathTheFallen());

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rhox.getRegenerationShield()).isEqualTo(1);

        declareBlockers(rhox, blocker);

        resolveCombat();

        // Rhox has "assign as though unblocked" — assign all damage to defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 5));

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player1, "Rhox");
        Permanent survivingRhox = findPermanent(player1, "Rhox");
        assertThat(survivingRhox.getRegenerationShield()).isZero();
        assertThat(survivingRhox.isTapped()).isTrue();
        assertThat(survivingRhox.isAttacking()).isFalse();
        assertThat(survivingRhox.getMarkedDamage()).isZero();
    }

    private void declareBlockers(Permanent attacker, Permanent... blockers) {
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(blockers).stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex))
                .toList());
    }
}
