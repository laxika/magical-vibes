package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndLockTargetPermanentWhileTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a target tap and the target's activated-ability lock while it remains tapped. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TapAndLockTargetPermanentWhileTappedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TapUntapSupport tapUntapSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapAndLockTargetPermanentWhileTappedEffect.class;
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

        tapUntapSupport.tapPermanent(gameData, target);
        LockTargetPermanentEffect lock = new LockTargetPermanentEffect(
                false, false, true, EffectDuration.WHILE_SOURCE_TAPPED, TargetPredicates.permanent());
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), target.getId(), entry.getControllerId(),
                lock, target.getId(), null, null, EffectDuration.WHILE_SOURCE_TAPPED, 0));

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                "'s activated abilities can't be activated for as long as it remains tapped."));
        log.info("Game {} - {} locks {} while tapped", gameData.id,
                entry.getCard().getName(), target.getCard().getName());
    }
}
