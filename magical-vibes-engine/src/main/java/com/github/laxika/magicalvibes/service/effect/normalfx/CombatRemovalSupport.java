package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CombatRemovalSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    public void removeFromCombat(GameData gameData, StackEntry entry, Permanent permanent) {
        if (permanent.isAttacking()) {
            permanent.setAttacking(false);
            permanent.setAttackTarget(null);
        }
        if (permanent.isBlocking()) {
            for (UUID attackerId : permanent.getBlockingTargetIds()) {
                Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
                if (attacker != null && attacker.isAttacking() && !hasOtherBlocker(gameData, attackerId, permanent.getId())) {
                    attacker.setBlockedWithoutBlockers(true);
                }
            }
            permanent.setBlocking(false);
            permanent.getBlockingTargets().clear();
            permanent.getBlockingTargetIds().clear();
        }

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " removes ", permanent.getCard(), " from combat."));
        log.info("Game {} - {} removes {} from combat", gameData.id, entry.getCard().getName(), permanent.getCard().getName());
    }

    public void removeFromCombatAndUnblockSoleBlockers(GameData gameData, StackEntry entry,
                                                       Permanent permanent) {
        List<UUID> previouslyBlockedAttackerIds = new ArrayList<>(permanent.getBlockingTargetIds());
        removeFromCombat(gameData, entry, permanent);

        for (UUID attackerId : previouslyBlockedAttackerIds) {
            Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
            if (attacker == null || !attacker.isAttacking()
                    || hadAnotherBlockerThisCombat(gameData, attackerId, permanent.getId())
                    || hasOtherBlocker(gameData, attackerId, permanent.getId())) {
                continue;
            }
            attacker.setBlockedWithoutBlockers(false);
        }
    }

    private boolean hadAnotherBlockerThisCombat(GameData gameData, UUID attackerId, UUID removedBlockerId) {
        Set<UUID> blockers = gameData.combatBlockOpponentIdsThisCombat.get(attackerId);
        return blockers != null && blockers.stream().anyMatch(id -> !id.equals(removedBlockerId));
    }

    private boolean hasOtherBlocker(GameData gameData, UUID attackerId, UUID removedBlockerId) {
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .anyMatch(permanent -> !permanent.getId().equals(removedBlockerId)
                        && permanent.isBlocking()
                        && permanent.getBlockingTargetIds().contains(attackerId));
    }
}
