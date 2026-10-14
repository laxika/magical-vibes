package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.PlaceClashCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves a clash, then inserts the library placement of the revealed cards (decided in APNAP
 * order and moved together, CR 701.30c) and any win reward into the current stack entry.
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
        UUID controllerId = entry.getControllerId();
        boolean won = triggerCollectionService.performClash(gameData, controllerId);
        gameData.lastClashWonByController.put(controllerId, won);
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst().orElse(null);
        List<CardEffect> followUps = new ArrayList<>();
        followUps.add(new PlaceClashCardsEffect(
                PlaceClashCardsEffectHandler.apnapOrder(gameData, controllerId, opponentId)));
        if (won && e.onWin() != null) followUps.add(e.onWin());
        if (won && e.repeatWhileWinning()) followUps.add(e);
        // A repeat re-inserts an equal ClashEffect record, so locate this one by the resolving index
        // rather than indexOf (which would find the first, already-resolved copy).
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, followUps);
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
