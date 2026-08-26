package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSelfFromCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveSelfFromCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveSelfFromCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : entry.getTargetId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        if (source.isAttacking()) {
            source.setAttacking(false);
            source.setAttackTarget(null);
        }
        if (source.isBlocking()) {
            for (UUID attackerId : source.getBlockingTargetIds()) {
                Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
                if (attacker != null && attacker.isAttacking()) {
                    attacker.setBlockedWithoutBlockers(true);
                }
            }
            source.setBlocking(false);
            source.getBlockingTargets().clear();
            source.getBlockingTargetIds().clear();
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " removes itself from combat."));
        log.info("Game {} - {} removes itself from combat", gameData.id, entry.getCard().getName());
    }
}
