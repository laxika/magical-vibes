package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves upkeep abilities that remove a counter from the source or sacrifice it. */
@Component
@RequiredArgsConstructor
public class RemoveCounterOrSacrificeSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final SacrificeSelfEffectHandler sacrificeSelfEffectHandler;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterOrSacrificeSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterOrSacrificeSelfEffect) effect;
        UUID sourceId = entry.getSourcePermanentId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        int current = source.getCounterCount(e.counterType());
        if (current > 0) {
            source.setCounterCount(e.counterType(), current - 1);
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    " loses a " + permanentCounterSupport.counterTypeName(e.counterType()) + " counter."));
            return;
        }

        if (e.thenEffects().isEmpty()) {
            sacrificeSelfEffectHandler.resolve(gameData, entry, new SacrificeSelfEffect());
            return;
        }

        UUID enchantedId = source.isAttached() ? source.getAttachedTo() : null;
        sacrificeSelfEffectHandler.resolve(gameData, entry, new SacrificeSelfEffect());
        if (enchantedId == null || gameQueryService.findPermanentById(gameData, enchantedId) == null) {
            return;
        }

        UUID originalTargetId = entry.getTargetId();
        entry.setTargetId(enchantedId);
        try {
            for (CardEffect thenEffect : e.thenEffects()) {
                EffectHandler handler = effectHandlerRegistry.getHandler(thenEffect);
                if (handler != null) {
                    handler.resolve(gameData, entry, thenEffect);
                }
            }
        } finally {
            entry.setTargetId(originalTargetId);
        }
    }
}
