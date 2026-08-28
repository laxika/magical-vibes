package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CombatTapCostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Validates and pays static combat costs that tap creatures. The engine currently has no wire
 * selection for these costs, so the first eligible creature in battlefield order is chosen.
 */
@Service
@RequiredArgsConstructor
public class CombatTapCostService {

    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    public boolean canPayAttackCost(GameData gameData, Permanent attacker) {
        Set<UUID> declaredIds = Set.of(attacker.getId());
        return availableTapSourceCount(gameData, attackerController(gameData, attacker), declaredIds)
                >= requiredTapCount(attacker);
    }

    public boolean canPayAttackCosts(GameData gameData, UUID playerId, Collection<Permanent> attackers) {
        Set<UUID> declaredIds = idsOf(attackers);
        int required = attackers.stream().mapToInt(this::requiredTapCount).sum();
        return availableTapSourceCount(gameData, playerId, declaredIds) >= required;
    }

    public boolean canPayBlockCost(GameData gameData, Permanent blocker) {
        Set<UUID> declaredIds = new HashSet<>();
        gameData.forEachPermanent((ignored, permanent) -> {
            if (permanent.isAttacking() || permanent.isBlocking()) {
                declaredIds.add(permanent.getId());
            }
        });
        declaredIds.add(blocker.getId());
        return availableTapSourceCount(gameData, attackerController(gameData, blocker), declaredIds)
                >= requiredTapCount(blocker);
    }

    public void validateAttackCosts(GameData gameData, UUID playerId, List<Permanent> attackers) {
        if (!canPayAttackCosts(gameData, playerId, attackers)) {
            throw new IllegalStateException("Not enough untapped creatures to attack");
        }
    }

    public void payAttackCosts(GameData gameData, UUID playerId, List<Permanent> attackers) {
        Set<UUID> declaredIds = idsOf(attackers);
        tapSources(gameData, playerId, declaredIds, attackers.stream().mapToInt(this::requiredTapCount).sum(),
                "attack");
    }

    public void validateBlockCosts(GameData gameData, UUID playerId, List<Permanent> attackingPermanents,
                                   Collection<Permanent> blockers) {
        Set<UUID> declaredIds = idsOf(attackingPermanents);
        blockers.forEach(blocker -> declaredIds.add(blocker.getId()));
        int required = uniquePermanents(blockers).stream().mapToInt(this::requiredTapCount).sum();
        validateAvailable(gameData, playerId, declaredIds, required, "block");
    }

    public void payBlockCosts(GameData gameData, UUID playerId, List<Permanent> attackingPermanents,
                              Collection<Permanent> blockers) {
        Set<UUID> declaredIds = idsOf(attackingPermanents);
        blockers.forEach(blocker -> declaredIds.add(blocker.getId()));
        int required = uniquePermanents(blockers).stream().mapToInt(this::requiredTapCount).sum();
        tapSources(gameData, playerId, declaredIds, required, "block");
    }

    private void validateAvailable(GameData gameData, UUID playerId, Set<UUID> declaredIds,
                                   int required, String action) {
        if (availableTapSourceCount(gameData, playerId, declaredIds) < required) {
            throw new IllegalStateException("Not enough untapped creatures to " + action);
        }
    }

    private void tapSources(GameData gameData, UUID playerId, Set<UUID> declaredIds,
                            int required, String action) {
        Set<UUID> usedIds = new HashSet<>();
        for (int i = 0; i < required; i++) {
            Permanent source = findTapSource(gameData, playerId, declaredIds, usedIds);
            if (source == null) {
                throw new IllegalStateException("Not enough untapped creatures to " + action);
            }
            source.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, source);
            usedIds.add(source.getId());
        }
    }

    private int availableTapSourceCount(GameData gameData, UUID playerId, Set<UUID> declaredIds) {
        int count = 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        for (Permanent permanent : battlefield) {
            if (isEligibleTapSource(gameData, permanent, declaredIds, Set.of())) {
                count++;
            }
        }
        return count;
    }

    private Permanent findTapSource(GameData gameData, UUID playerId, Set<UUID> declaredIds,
                                    Set<UUID> usedIds) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (isEligibleTapSource(gameData, permanent, declaredIds, usedIds)) {
                return permanent;
            }
        }
        return null;
    }

    private boolean isEligibleTapSource(GameData gameData, Permanent permanent, Set<UUID> declaredIds,
                                        Set<UUID> usedIds) {
        return gameQueryService.isCreature(gameData, permanent)
                && !permanent.isTapped()
                && !permanent.isAttacking()
                && !permanent.isBlocking()
                && !declaredIds.contains(permanent.getId())
                && !usedIds.contains(permanent.getId());
    }

    private int requiredTapCount(Permanent permanent) {
        return permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(CombatTapCostEffect.class::isInstance)
                .map(CombatTapCostEffect.class::cast)
                .mapToInt(CombatTapCostEffect::tapCount)
                .sum();
    }

    private UUID attackerController(GameData gameData, Permanent permanent) {
        return gameQueryService.findPermanentController(gameData, permanent.getId());
    }

    private Set<UUID> idsOf(Collection<Permanent> permanents) {
        Set<UUID> ids = new LinkedHashSet<>();
        permanents.forEach(permanent -> ids.add(permanent.getId()));
        return ids;
    }

    private List<Permanent> uniquePermanents(Collection<Permanent> permanents) {
        Set<UUID> seen = new HashSet<>();
        List<Permanent> unique = new ArrayList<>();
        for (Permanent permanent : permanents) {
            if (seen.add(permanent.getId())) {
                unique.add(permanent);
            }
        }
        return unique;
    }
}
