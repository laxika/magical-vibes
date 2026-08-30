package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMysticReflection;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterMysticReflectionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Mystic Reflection's delayed battlefield-entry replacement. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterMysticReflectionEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterMysticReflectionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        gameData.pendingMysticReflections.add(
                new PendingMysticReflection(targetId, target.getCard()));
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getCard().getName() + " will make the next creatures or planeswalkers enter as copies of ",
                target.getCard(), "."));
        log.info("Game {} - Mystic Reflection registers a replacement copying {}", gameData.id,
                target.getCard().getName());
    }
}
