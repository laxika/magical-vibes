package com.github.laxika.magicalvibes.model;

import java.util.*;

/**
 * Encapsulates all state from combat damage phase 1 (first strike) that's needed for phase 2.
 */
public class CombatDamagePhase1State {

    public final Set<Integer> deadAttackerIndices;
    public final Set<Integer> deadDefenderIndices;
    public final Map<Integer, Integer> atkDamageTaken;
    public final Map<Integer, Integer> defDamageTaken;
    public final Map<UUID, Integer> combatDamageDealt;
    public final Map<UUID, Integer> combatDamageDealtToPlayer;
    public final Map<UUID, List<UUID>> combatDamageDealtToCreatures;
    public final Map<UUID, UUID> combatDamageDealerControllers;
    public final int damageToDefendingPlayer;
    public final int damageRedirectedToGuard;
    public final Map<Integer, List<Integer>> blockerMap;
    public final boolean anyFirstStrike;

    public CombatDamagePhase1State(
            Set<Integer> deadAttackerIndices,
            Set<Integer> deadDefenderIndices,
            Map<Integer, Integer> atkDamageTaken,
            Map<Integer, Integer> defDamageTaken,
            Map<UUID, Integer> combatDamageDealt,
            Map<UUID, Integer> combatDamageDealtToPlayer,
            Map<UUID, List<UUID>> combatDamageDealtToCreatures,
            Map<UUID, UUID> combatDamageDealerControllers,
            int damageToDefendingPlayer,
            int damageRedirectedToGuard,
            Map<Integer, List<Integer>> blockerMap,
            boolean anyFirstStrike) {
        this.deadAttackerIndices = deadAttackerIndices;
        this.deadDefenderIndices = deadDefenderIndices;
        this.atkDamageTaken = atkDamageTaken;
        this.defDamageTaken = defDamageTaken;
        this.combatDamageDealt = combatDamageDealt;
        this.combatDamageDealtToPlayer = combatDamageDealtToPlayer;
        this.combatDamageDealtToCreatures = combatDamageDealtToCreatures;
        this.combatDamageDealerControllers = combatDamageDealerControllers;
        this.damageToDefendingPlayer = damageToDefendingPlayer;
        this.damageRedirectedToGuard = damageRedirectedToGuard;
        this.blockerMap = blockerMap;
        this.anyFirstStrike = anyFirstStrike;
    }
}
