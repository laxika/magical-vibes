package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.*;

/**
 * Mutable state object that tracks all combat damage across both phases (first strike and regular).
 * Groups the ~15 local variables previously scattered throughout resolveCombatDamage().
 */
class CombatDamageState {

    // Player damage accumulation
    int damageToDefendingPlayer;
    int poisonDamageToDefendingPlayer;
    /**
     * The part of {@link #damageToDefendingPlayer} dealt by sources whose damage can't be prevented
     * (Malignus). The aggregate prevention chain in {@code applyPlayerDamage} may never reduce the
     * player's damage below this floor.
     */
    int unpreventableDamageToDefendingPlayer;
    int damageRedirectedToGuard;
    int infectDamageRedirectedToGuard;
    boolean deathtouchDamageRedirectedToGuard;

    // Death tracking (reverse order for safe index-based removal)
    final Set<Integer> deadAttackerIndices = new TreeSet<>(Collections.reverseOrder());
    final Set<Integer> deadDefenderIndices = new TreeSet<>(Collections.reverseOrder());

    // Cumulative damage on each creature (index -> damage)
    final Map<Integer, Integer> atkDamageTaken = new HashMap<>();
    final Map<Integer, Integer> defDamageTaken = new HashMap<>();

    // Per-source contributions to the above (index -> source permanent id -> damage), for
    // CantBeDestroyedByLethalDamageUnlessSingleSourceEffect tracking through combat prevention.
    final Map<Integer, Map<UUID, Integer>> atkDamageTakenBySource = new HashMap<>();
    final Map<Integer, Map<UUID, Integer>> defDamageTakenBySource = new HashMap<>();

    // The part of the above dealt by sources whose damage can't be prevented (Malignus): a per-index
    // floor the creature prevention shields may never reduce that step's damage below.
    final Map<Integer, Integer> unpreventableAtkDamageTaken = new HashMap<>();
    final Map<Integer, Integer> unpreventableDefDamageTaken = new HashMap<>();

    // Deathtouch tracking
    final Set<Integer> deathtouchDamagedAttackerIndices = new HashSet<>();
    final Set<Integer> deathtouchDamagedDefenderIndices = new HashSet<>();

    // Planeswalker damage accumulation (planeswalker UUID -> damage)
    final Map<UUID, Integer> damageToPlaneswalkers = new HashMap<>();

    // Combat damage records (for triggers: lifelink, combat damage to player, etc.)
    final Map<Permanent, Integer> combatDamageDealt = new HashMap<>();
    final Map<Permanent, Integer> combatDamageDealtToPlayer = new HashMap<>();
    final Map<Permanent, Integer> combatDamageDealtToPlaneswalker = new HashMap<>();
    final Map<Permanent, List<UUID>> combatDamageDealtToCreatures = new HashMap<>();
    final Map<Permanent, UUID> combatDamageDealerControllers = new HashMap<>();
    final Map<Permanent, List<CardEffect>> selfDealsCombatDamageEffects = new HashMap<>();
    final List<StackEntry> enchantedCreatureDealsDamageTriggers = new ArrayList<>();

    // Per-source damage amounts to each target creature (for ON_DEALT_DAMAGE triggers needing damage amount)
    // Key: source permanent, Value: map of target creature ID -> damage amount
    final Map<Permanent, Map<UUID, Integer>> combatDamageAmountsToCreatures = new HashMap<>();

    // Controller of each damaged creature, captured while it is still alive so reflection triggers
    // (ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE) can find it after the creature dies. Key: damaged
    // creature ID, Value: its controller ID at damage time.
    final Map<UUID, UUID> combatDamageTargetControllers = new HashMap<>();

    // CR 510.1 — Snapshot of whether defender's damage should be dealt as infect (Phyrexian Unlife),
    // captured before lifelink is processed so simultaneous combat damage uses pre-damage life total.
    boolean defenderDamageAsInfect;
}
