package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shared combat-damage division used by live AI answers and simulation auto-play.
 * Prefers killing the most threatening killable creatures (by effective power) rather than
 * assigning lethal in presentation order.
 */
public final class CombatDamageAssignmentHeuristic {

    private CombatDamageAssignmentHeuristic() {
    }

    public static Map<UUID, Integer> assign(PendingInteraction.CombatDamageAssignment interaction,
                                            GameData gameData,
                                            GameQueryService gameQueryService) {
        if (interaction.singleRecipient()) {
            return assignSingleRecipient(interaction, gameData, gameQueryService);
        }

        Map<UUID, Integer> assignments = new HashMap<>();
        int remaining = interaction.totalDamage();
        boolean deathtouch = interaction.isDeathtouch();

        List<CombatDamageTarget> creatures = new ArrayList<>();
        CombatDamageTarget overflow = null;
        for (CombatDamageTarget target : interaction.validTargets()) {
            if (target.isPlayer()) {
                overflow = target;
            } else {
                creatures.add(target);
            }
        }
        creatures.sort(Comparator
                .comparingInt((CombatDamageTarget t) -> threat(t, gameData, gameQueryService)).reversed()
                .thenComparingInt(t -> lethal(t, deathtouch)));

        // Kill the most threatening creatures we can afford, highest threat first.
        for (CombatDamageTarget target : creatures) {
            if (remaining <= 0) {
                break;
            }
            if (isIndestructible(target, gameData, gameQueryService)) {
                continue;
            }
            int need = lethal(target, deathtouch);
            if (need <= 0 || need > remaining) {
                continue;
            }
            assignments.merge(target.id(), need, Integer::sum);
            remaining -= need;
        }

        // Trample / overflow: only after every creature has been assigned lethal (CR 510.1c).
        if (remaining > 0 && overflow != null && allHaveLethal(creatures, assignments, deathtouch)) {
            assignments.put(overflow.id(), remaining);
            remaining = 0;
        }

        // Chip remaining damage onto the most threatening creatures that still need lethal.
        if (remaining > 0) {
            for (CombatDamageTarget target : creatures) {
                if (remaining <= 0) {
                    break;
                }
                int need = lethal(target, deathtouch);
                int have = assignments.getOrDefault(target.id(), 0);
                int deficit = Math.max(0, need - have);
                if (deficit <= 0) {
                    continue;
                }
                int dmg = Math.min(remaining, deficit);
                assignments.merge(target.id(), dmg, Integer::sum);
                remaining -= dmg;
            }
        }

        // Excess beyond all lethals with no overflow target: pile onto the top threat.
        if (remaining > 0 && !creatures.isEmpty()) {
            assignments.merge(creatures.getFirst().id(), remaining, Integer::sum);
            remaining = 0;
        }
        if (remaining > 0 && overflow != null) {
            assignments.merge(overflow.id(), remaining, Integer::sum);
        }

        return assignments;
    }

    private static Map<UUID, Integer> assignSingleRecipient(PendingInteraction.CombatDamageAssignment interaction,
                                                            GameData gameData,
                                                            GameQueryService gameQueryService) {
        int totalDamage = interaction.totalDamage();
        CombatDamageTarget bestKill = null;
        int bestThreat = Integer.MIN_VALUE;
        CombatDamageTarget player = null;

        for (CombatDamageTarget target : interaction.validTargets()) {
            if (target.isPlayer()) {
                player = target;
                continue;
            }
            if (isIndestructible(target, gameData, gameQueryService)) {
                continue;
            }
            int need = lethal(target, interaction.isDeathtouch());
            if (need <= 0 || need > totalDamage) {
                continue;
            }
            int threat = threat(target, gameData, gameQueryService);
            if (threat > bestThreat) {
                bestThreat = threat;
                bestKill = target;
            }
        }

        CombatDamageTarget chosen = bestKill != null
                ? bestKill
                : player != null ? player : interaction.validTargets().getFirst();
        return Map.of(chosen.id(), totalDamage);
    }

    private static boolean allHaveLethal(List<CombatDamageTarget> creatures,
                                         Map<UUID, Integer> assignments,
                                         boolean deathtouch) {
        for (CombatDamageTarget target : creatures) {
            if (assignments.getOrDefault(target.id(), 0) < lethal(target, deathtouch)) {
                return false;
            }
        }
        return true;
    }

    private static int lethal(CombatDamageTarget target, boolean deathtouch) {
        if (deathtouch) {
            return Math.max(0, 1 - target.currentDamage());
        }
        return Math.max(0, target.effectiveToughness() - target.currentDamage());
    }

    private static int threat(CombatDamageTarget target, GameData gameData, GameQueryService gameQueryService) {
        if (gameData != null && gameQueryService != null) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, target.id());
            if (permanent != null) {
                return gameQueryService.getEffectivePower(gameData, permanent);
            }
        }
        // Fallback when the permanent cannot be resolved (simulation edge cases).
        return Math.max(0, target.effectiveToughness() - target.currentDamage());
    }

    private static boolean isIndestructible(CombatDamageTarget target, GameData gameData,
                                            GameQueryService gameQueryService) {
        if (gameData == null || gameQueryService == null) {
            return false;
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, target.id());
        return permanent != null && gameQueryService.hasKeyword(gameData, permanent, Keyword.INDESTRUCTIBLE);
    }
}
