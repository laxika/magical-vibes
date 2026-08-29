package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SwapBlockingAssignmentsBetweenTwoCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityContext;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Sorrow's Path by exchanging two blocking creatures' current blocking assignments.
 * Rechecks blocking legality and capacity at resolution and changes the assignments directly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwapBlockingAssignmentsBetweenTwoCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final BlockLegalityService blockLegalityService;
    private final CombatBlockService combatBlockService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SwapBlockingAssignmentsBetweenTwoCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds == null || targetIds.size() < 2) {
            return;
        }

        Permanent blockerA = gameQueryService.findPermanentById(gameData, targetIds.get(0));
        Permanent blockerB = gameQueryService.findPermanentById(gameData, targetIds.get(1));
        if (!validTargets(gameData, entry, blockerA, blockerB)) {
            return;
        }

        List<Permanent> attackersBlockedByA = findAttackingCreatures(gameData, blockerA);
        List<Permanent> attackersBlockedByB = findAttackingCreatures(gameData, blockerB);
        if (!canBlockAll(gameData, blockerA, attackersBlockedByB)
                || !canBlockAll(gameData, blockerB, attackersBlockedByA)) {
            log.info("Game {} - {} swap does nothing: mutual block legality failed",
                    gameData.id, entry.getCard().getName());
            return;
        }

        if (!haveBattlefieldIndices(gameData, attackersBlockedByA)
                || !haveBattlefieldIndices(gameData, attackersBlockedByB)) {
            return;
        }

        reassignBlocker(gameData, blockerA, attackersBlockedByB);
        reassignBlocker(gameData, blockerB, attackersBlockedByA);

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" switches the blocking assignments of ")
                .card(blockerA.getCard())
                .text(" and ")
                .card(blockerB.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} switches the blocking assignments of {} and {}",
                gameData.id, entry.getCard().getName(), blockerA.getCard().getName(),
                blockerB.getCard().getName());
    }

    private boolean validTargets(GameData gameData, StackEntry entry, Permanent blockerA, Permanent blockerB) {
        if (blockerA == null || blockerB == null || blockerA.getId().equals(blockerB.getId())
                || !gameQueryService.isCreature(gameData, blockerA)
                || !gameQueryService.isCreature(gameData, blockerB)
                || !blockerA.isBlocking() || !blockerB.isBlocking()) {
            return false;
        }

        UUID controllerA = gameQueryService.findPermanentController(gameData, blockerA.getId());
        UUID controllerB = gameQueryService.findPermanentController(gameData, blockerB.getId());
        return controllerA != null && controllerA.equals(controllerB)
                && !controllerA.equals(entry.getControllerId());
    }

    private List<Permanent> findAttackingCreatures(GameData gameData, Permanent blocker) {
        List<Permanent> attackers = new ArrayList<>();
        for (UUID attackerId : blocker.getBlockingTargetIds()) {
            Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
            if (attacker != null && attacker.isAttacking() && gameQueryService.isCreature(gameData, attacker)) {
                attackers.add(attacker);
            }
        }
        return attackers;
    }

    private boolean canBlockAll(GameData gameData, Permanent blocker, List<Permanent> attackers) {
        if (attackers.isEmpty()) {
            return true;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, blocker.getId());
        List<Permanent> defenderBattlefield = controllerId == null
                ? List.of()
                : gameData.playerBattlefields.getOrDefault(controllerId, List.of());
        if (attackers.size() > combatBlockService.getMaxBlocksForCreature(gameData, blocker, defenderBattlefield)) {
            return false;
        }

        BlockLegalityContext context = blockLegalityService.createBlockLegalityContext(gameData, defenderBattlefield);
        for (Permanent attacker : attackers) {
            if (!blockLegalityService.canBlockAttacker(context, blocker, attacker)) {
                return false;
            }
        }
        return true;
    }

    private boolean haveBattlefieldIndices(GameData gameData, List<Permanent> attackers) {
        for (Permanent attacker : attackers) {
            if (attackerBattlefieldIndex(gameData, attacker) < 0) {
                return false;
            }
        }
        return true;
    }

    private void reassignBlocker(GameData gameData, Permanent blocker, List<Permanent> attackers) {
        blocker.setBlocking(false);
        blocker.getBlockingTargets().clear();
        blocker.getBlockingTargetIds().clear();
        for (Permanent attacker : attackers) {
            blocker.addBlockingTarget(attackerBattlefieldIndex(gameData, attacker));
            blocker.addBlockingTargetId(attacker.getId());
        }
        if (!attackers.isEmpty()) {
            blocker.setBlocking(true);
        }
    }

    private int attackerBattlefieldIndex(GameData gameData, Permanent attacker) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        if (controllerId == null) {
            return -1;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        return battlefield == null ? -1 : battlefield.indexOf(attacker);
    }
}
