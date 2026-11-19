package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantBecomeUntappedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link CantBecomeUntappedEffect} with {@link GrantScope#TARGET}: "that creature can't become untapped for as
 * long as you control [this]" (Spider-Woman, Secret Agent). Each target gets a floating effect with this record as its
 * payload, {@code Duration.WHILE_SOURCE_CONTROLLED}; no effect untaps it while it lasts, the untap step included. A
 * source its controller no longer controls never starts the duration (CR 611.2b). The {@code SELF} and
 * {@code ENCHANTED} scopes are static abilities and never resolve.
 */
@Component
@RequiredArgsConstructor
public class CantBecomeUntappedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantBecomeUntappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (((CantBecomeUntappedEffect) effect).scope() != GrantScope.TARGET) {
            return;
        }
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null || !entry.getControllerId().equals(
                gameQueryService.findPermanentController(gameData, sourcePermanentId))) {
            return;
        }
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            gameData.addEffectOnPermanent(entry, effect, targetId, Duration.WHILE_SOURCE_CONTROLLED);
            gameLogService.append(gameData, GameLog.cardTextCard(target.getCard(),
                    " can't become untapped for as long as its controller controls ", entry.getCard(), "."));
        }
    }
}
