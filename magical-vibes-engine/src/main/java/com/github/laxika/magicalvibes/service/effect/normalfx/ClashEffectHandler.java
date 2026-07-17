package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ClashEffect}: each iteration dispatches the {@code beforeClash} effects in
 * order, performs the clash for the controller via {@link TriggerCollectionService#performClash},
 * and on a win dispatches the {@code onWin} effect against the same stack entry (so it acts on
 * the source permanent). With {@code repeatWhileWinning} the whole sequence repeats until the
 * controller loses a clash (or decks out, which counts as a loss in {@code performClash}).
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

        boolean won;
        do {
            for (CardEffect beforeEffect : e.beforeClash()) {
                dispatch(gameData, entry, beforeEffect);
            }

            won = triggerCollectionService.performClash(gameData, entry.getControllerId());
            // Record the result so a later effect on the same stack entry can branch on it via the
            // WonClash condition (e.g. Whirlpool Whelm's optional "put on top of library instead").
            gameData.lastClashWonByController.put(entry.getControllerId(), won);

            if (won && e.onWin() != null) {
                dispatch(gameData, entry, e.onWin());
            }
        } while (won && e.repeatWhileWinning());
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        // SequenceEffect has no handler of its own — expand it here so a multi-step win reward
        // (e.g. Sentry Oak's "+2/+0 and loses defender") resolves each step in order against the
        // same entry. ClashEffect dispatches synchronously, so sequence steps must be synchronous
        // (no async player-input pauses); the ordinary resolution-loop splice covers wrappers that
        // can pause.
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(effect);
        if (handler != null) {
            handler.resolve(gameData, entry, effect);
        } else {
            log.warn("No handler for effect in ClashEffect: {}", effect.getClass().getSimpleName());
        }
    }
}
