package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SwapExclusiveBlockersBetweenTwoBlockedAttackersEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BlockLegalityContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SwapExclusiveBlockersBetweenTwoBlockedAttackersEffect} (General Jarkeld).
 * Reads two blocked attackers from {@code targetIds}. Re-checks the mutual "could be blocked by
 * all creatures that the other is blocked by" condition, then swaps exclusive blockers only.
 * Does not re-fire block / become-blocked triggers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwapExclusiveBlockersBetweenTwoBlockedAttackersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SwapExclusiveBlockersBetweenTwoBlockedAttackersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds == null || targetIds.size() < 2) {
            return;
        }

        Permanent attackerA = gameQueryService.findPermanentById(gameData, targetIds.get(0));
        Permanent attackerB = gameQueryService.findPermanentById(gameData, targetIds.get(1));
        if (attackerA == null || attackerB == null
                || !attackerA.isAttacking() || !attackerB.isAttacking()) {
            return;
        }

        List<Permanent> blockersOfA = findBlockers(gameData, attackerA.getId());
        List<Permanent> blockersOfB = findBlockers(gameData, attackerB.getId());
        if (blockersOfA.isEmpty() || blockersOfB.isEmpty()) {
            // Targets must still be blocked; otherwise the effect does nothing.
            return;
        }

        if (!mutualBlockLegalityHolds(gameData, attackerA, blockersOfA, attackerB, blockersOfB)) {
            log.info("Game {} - {} swap does nothing: mutual block legality failed",
                    gameData.id, entry.getCard().getName());
            return;
        }

        int indexA = attackerBattlefieldIndex(gameData, attackerA);
        int indexB = attackerBattlefieldIndex(gameData, attackerB);
        if (indexA < 0 || indexB < 0) {
            return;
        }

        List<Permanent> exclusiveA = exclusiveBlockers(blockersOfA, attackerB.getId());
        List<Permanent> exclusiveB = exclusiveBlockers(blockersOfB, attackerA.getId());

        for (Permanent blocker : exclusiveA) {
            reassignBlocker(blocker, attackerA.getId(), attackerB.getId(), indexB);
        }
        for (Permanent blocker : exclusiveB) {
            reassignBlocker(blocker, attackerB.getId(), attackerA.getId(), indexA);
        }

        String msg = entry.getCard().getName() + " switches blockers between "
                + attackerA.getCard().getName() + " and " + attackerB.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" switches blockers between ")
                .card(attackerA.getCard())
                .text(" and ")
                .card(attackerB.getCard())
                .text(".")
                .build());
        log.info("Game {} - {}", gameData.id, msg);
    }

    private List<Permanent> findBlockers(GameData gameData, UUID attackerId) {
        List<Permanent> blockers = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(attackerId)) {
                blockers.add(permanent);
            }
        });
        return blockers;
    }

    private boolean mutualBlockLegalityHolds(
            GameData gameData,
            Permanent attackerA, List<Permanent> blockersOfA,
            Permanent attackerB, List<Permanent> blockersOfB) {
        // Check each blocker against the other attacker using that blocker's controller battlefield.
        for (Permanent blocker : blockersOfB) {
            if (!canBlock(gameData, blocker, attackerA)) {
                return false;
            }
        }
        for (Permanent blocker : blockersOfA) {
            if (!canBlock(gameData, blocker, attackerB)) {
                return false;
            }
        }
        return true;
    }

    private boolean canBlock(GameData gameData, Permanent blocker, Permanent attacker) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, blocker.getId());
        List<Permanent> defenderBattlefield = controllerId == null
                ? List.of()
                : gameData.playerBattlefields.getOrDefault(controllerId, List.of());
        BlockLegalityContext context = gameQueryService.createBlockLegalityContext(gameData, defenderBattlefield);
        return gameQueryService.canBlockAttacker(context, blocker, attacker);
    }

    private List<Permanent> exclusiveBlockers(List<Permanent> blockersOfOne, UUID otherAttackerId) {
        List<Permanent> exclusive = new ArrayList<>();
        for (Permanent blocker : blockersOfOne) {
            if (!blocker.getBlockingTargetIds().contains(otherAttackerId)) {
                exclusive.add(blocker);
            }
        }
        return exclusive;
    }

    private int attackerBattlefieldIndex(GameData gameData, Permanent attacker) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        if (controllerId == null) {
            return -1;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        return battlefield == null ? -1 : battlefield.indexOf(attacker);
    }

    private void reassignBlocker(Permanent blocker, UUID fromAttackerId, UUID toAttackerId, int toIndex) {
        List<UUID> ids = blocker.getBlockingTargetIds();
        List<Integer> indices = blocker.getBlockingTargets();
        for (int i = 0; i < ids.size(); i++) {
            if (fromAttackerId.equals(ids.get(i))) {
                ids.set(i, toAttackerId);
                if (i < indices.size()) {
                    indices.set(i, toIndex);
                } else {
                    while (indices.size() < i) {
                        indices.add(-1);
                    }
                    indices.add(toIndex);
                }
            }
        }
    }
}
