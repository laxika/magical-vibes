package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatWhileWinningClashEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RepeatWhileWinningClashEffect}: "[body], then clash with an opponent. If you win,
 * repeat this process." Each iteration dispatches the body effects in order for the controller, then
 * clashes via {@link TriggerCollectionService#performClash}; while the controller keeps winning, the
 * whole sequence repeats. The loop terminates naturally once the controller loses a clash (or decks
 * out, which counts as a loss in {@code performClash}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RepeatWhileWinningClashEffectHandler implements NormalEffectHandlerBean {

    private final TriggerCollectionService triggerCollectionService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RepeatWhileWinningClashEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RepeatWhileWinningClashEffect) effect;

        boolean won;
        do {
            for (CardEffect bodyEffect : e.body()) {
                EffectHandler handler = effectHandlerRegistry.getHandler(bodyEffect);
                if (handler != null) {
                    handler.resolve(gameData, entry, bodyEffect);
                } else {
                    log.warn("No handler for body effect in RepeatWhileWinningClashEffect: {}",
                            bodyEffect.getClass().getSimpleName());
                }
            }

            won = triggerCollectionService.performClash(gameData, entry.getControllerId());
            gameData.lastClashWonByController.put(entry.getControllerId(), won);
        } while (won);
    }
}
