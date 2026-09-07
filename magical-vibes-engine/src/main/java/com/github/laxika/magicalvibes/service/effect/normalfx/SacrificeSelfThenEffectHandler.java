package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sacrifices the source permanent and then — only if the sacrifice succeeded ("if you do") —
 * resolves the payload against the same stack entry. The sacrifice itself matches
 * {@code SacrificeSelfEffectHandler}: it fires ally-sacrifice triggers and cleans up the Auras the
 * departing permanent leaves behind.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeSelfThenEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfThenEffect) effect;

        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles — source no longer on the battlefield."));
            return;
        }

        if (!entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, self.getId()))) {
            return;
        }

        if (!permanentRemovalService.removePermanentToGraveyard(gameData, self)) {
            return;
        }

        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, entry.getControllerId(), self.getCard());
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        dispatch(gameData, entry, e.reflexive()
                ? new QueueReflexiveAbilityEffect(e.thenEffect())
                : e.thenEffect());
    }

    /**
     * Resolves the payload through its own handler against this entry. {@link SequenceEffect} has no
     * handler of its own, so a multi-step payload is expanded here — dispatch is synchronous, so its
     * steps must be synchronous too, exactly as in {@code FlipCoinWinEffectHandler}.
     */
    private void dispatch(GameData gameData, StackEntry entry, CardEffect payload) {
        if (payload instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(payload);
        if (handler != null) {
            handler.resolve(gameData, entry, payload);
        } else {
            log.warn("No handler for payload effect in SacrificeSelfThenEffect: {}",
                    payload.getClass().getSimpleName());
        }
    }
}
