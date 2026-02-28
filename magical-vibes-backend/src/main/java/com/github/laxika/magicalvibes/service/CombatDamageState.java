package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.*;

/**
 * Mutable state object that tracks all combat damage across both phases (first strike and regular).
 * Groups the ~15 local variables previously scattered throughout resolveCombatDamage().
 */
class CombatDamageState {

    // Player damage accumulation
    int damageToDefendingPlayer;
    int poisonDamageToDefendingPlayer;
    int damageRedirectedToGuard;
    int infectDamageRedirectedToGuard;

    // Death tracking (reverse order for safe index-based removal)
    final Set<Integer> deadAttackerIndices = new TreeSet<>(Collections.reverseOrder());
    final Set<Integer> deadDefenderIndices = new TreeSet<>(Collections.reverseOrder());

    // Cumulative damage on each creature (index -> damage)
    final Map<Integer, Integer> atkDamageTaken = new HashMap<>();
    final Map<Integer, Integer> defDamageTaken = new HashMap<>();

    // Deathtouch tracking
    final Set<Integer> deathtouchDamagedAttackerIndices = new HashSet<>();
    final Set<Integer> deathtouchDamagedDefenderIndices = new HashSet<>();

    // Combat damage records (for triggers: lifelink, combat damage to player, etc.)
    final Map<Permanent, Integer> combatDamageDealt = new HashMap<>();
    final Map<Permanent, Integer> combatDamageDealtToPlayer = new HashMap<>();
    final Map<Permanent, List<UUID>> combatDamageDealtToCreatures = new HashMap<>();
    final Map<Permanent, UUID> combatDamageDealerControllers = new HashMap<>();
}
