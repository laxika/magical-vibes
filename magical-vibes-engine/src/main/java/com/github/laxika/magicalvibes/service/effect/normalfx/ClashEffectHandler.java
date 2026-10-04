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
 * Resolves a clash, then inserts both library-placement choices and any win reward into the
 * current stack entry. Each placement choice can pause for input before the sequence continues.
 * Winning with {@code repeatWhileWinning} inserts another clash after those choices.
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

        for (CardEffect beforeEffect : e.beforeClash()) {
            dispatch(gameData, entry, beforeEffect);
        }
        boolean won = triggerCollectionService.performClash(gameData, entry.getControllerId());
        gameData.lastClashWonByController.put(entry.getControllerId(), won);
        java.util.List<CardEffect> followUps = new java.util.ArrayList<>();
        followUps.add(new com.github.laxika.magicalvibes.model.effect.ScryEffect(
                1, com.github.laxika.magicalvibes.model.effect.LibraryOwner.CONTROLLER, false));
        followUps.add(new com.github.laxika.magicalvibes.model.effect.ScryEffect(
                1, com.github.laxika.magicalvibes.model.effect.LibraryOwner.OPPONENT, false));
        if (won && e.onWin() != null) followUps.add(e.onWin());
        if (won && e.repeatWhileWinning()) followUps.add(e);
        int index = entry.getEffectsToResolve().indexOf(effect);
        entry.insertEffectsToResolve(index + 1, followUps);
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
