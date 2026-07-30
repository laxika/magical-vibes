package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link BecomeColorlessIndefinitelyEffect} by floating the layer-5 color setter with a
 * {@link EffectDuration#PERMANENT} duration, so the permanent stays colorless for as long as it
 * exists rather than reverting at cleanup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeColorlessIndefinitelyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeColorlessIndefinitelyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BecomeColorlessIndefinitelyEffect become = (BecomeColorlessIndefinitelyEffect) effect;
        UUID affectedId = become.targeted() || entry.getSourcePermanentId() == null
                ? entry.getTargetId()
                : entry.getSourcePermanentId();
        Permanent affected = gameQueryService.findPermanentById(gameData, affectedId);
        if (affected == null) {
            return;
        }

        affected.getTransientColors().clear();
        affected.setColorOverridden(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), null, entry.getControllerId(),
                new BecomeColorlessUntilEndOfTurnEffect(false),
                affected.getId(), null, null, EffectDuration.PERMANENT, 0));

        gameLogService.append(gameData, GameLog.cardThen(affected.getCard(), " becomes colorless."));
        log.info("Game {} - {} becomes colorless indefinitely", gameData.id, affected.getCard().getName());
    }
}
