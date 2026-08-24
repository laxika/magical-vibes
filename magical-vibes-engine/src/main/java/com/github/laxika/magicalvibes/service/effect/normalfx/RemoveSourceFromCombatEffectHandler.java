package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSourceFromCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveSourceFromCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveSourceFromCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
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

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " removes ", source.getCard(), " from combat."));
        log.info("Game {} - {} removes {} from combat", gameData.id, entry.getCard().getName(), source.getCard().getName());
    }
}
