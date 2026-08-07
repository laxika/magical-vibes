package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetAttackingCreatureBlockedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MakeTargetAttackingCreatureBlockedEffect}: the target attacking creature becomes
 * blocked with no creature blocking it (CR 509.1h), so it assigns no combat damage (CR 510.1c).
 * "Becomes blocked" triggers that don't depend on a blocker still fire.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MakeTargetAttackingCreatureBlockedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final CombatBlockService combatBlockService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeTargetAttackingCreatureBlockedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets.isEmpty()) {
            if (entry.getTargetId() == null) {
                return;
            }
            targets = List.of(entry.getTargetId());
        }

        for (UUID targetId : targets) {
            makeBlocked(gameData, gameQueryService.findPermanentById(gameData, targetId));
        }
    }

    private void makeBlocked(GameData gameData, Permanent target) {
        // An attacker already blocked by a creature is simply already blocked - no re-fire.
        if (target == null || !target.isAttacking() || target.isBlockedWithoutBlockers()
                || gameQueryService.isBlockedByAnyCreature(gameData, target)) {
            return;
        }

        target.setBlockedWithoutBlockers(true);

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " becomes blocked."));
        log.info("Game {} - {} becomes blocked with no blockers", gameData.id, target.getCard().getName());

        combatBlockService.fireBecomesBlockedTriggersWithoutBlockers(gameData, target);
    }
}
