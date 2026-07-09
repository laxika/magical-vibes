package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ClashEffect}: "Clash with an opponent. If you win, [wrapped effect]."
 * Performs the clash for the controller via {@link TriggerCollectionService#performClash}; on a win,
 * dispatches the wrapped effect against the same stack entry (so it acts on the source permanent).
 * Mirrors {@link FlipCoinWinEffectHandler}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClashEffectHandler implements NormalEffectHandlerBean {

    private final TriggerCollectionService triggerCollectionService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ClashEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ClashEffect) effect;

        boolean won = triggerCollectionService.performClash(gameData, entry.getControllerId());
        // Record the result so a later effect on the same stack entry can branch on it via the
        // WonClash condition (e.g. Whirlpool Whelm's optional "put on top of library instead").
        gameData.lastClashWonByController.put(entry.getControllerId(), won);
        if (!won || e.wrapped() == null) {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(e.wrapped());
        if (handler != null) {
            handler.resolve(gameData, entry, e.wrapped());
        } else {
            log.warn("No handler for wrapped effect in ClashEffect: {}",
                    e.wrapped().getClass().getSimpleName());
        }
    }
}
