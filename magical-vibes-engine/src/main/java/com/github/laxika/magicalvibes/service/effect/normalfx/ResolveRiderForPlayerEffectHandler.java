package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveRiderForPlayerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves a {@link ResolveRiderForPlayerEffect} step: its rider, on a stack entry derived from the resolving one
 * whose controller, target and event value are the step's, after checking a {@link ConditionalEffect} rider's
 * condition against that entry. A "then" handler inserts the step after itself only for a rider that
 * {@link #resolvesAsStep resolves as a step}; any other rider it still resolves at once.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst} for the riders whose own handler is: checking the condition changes
 * nothing, so a rider that asks its CR 616.1 questions before its first change (a life loss Lich's Duel Mastery's
 * sacrifice would replace) is stopped and resolved again as this step, after the handler's own change is done.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResolveRiderForPlayerEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ResolveRiderForPlayerEffect.class;
    }

    /**
     * Whether {@code rider} (a plain effect, or a resolution-time {@link ConditionalEffect} around one) is resolved
     * by a marked handler, so it can be a step of its own with its own rerun window.
     */
    public boolean resolvesAsStep(CardEffect rider) {
        CardEffect resolved = rider instanceof ConditionalEffect conditional && !conditional.triggerTimeOnly()
                ? conditional.wrapped() : rider;
        return !(resolved instanceof ConditionalEffect)
                && effectHandlerRegistry.getHandler(resolved) instanceof AsksDeathReplacementsFirst marked
                && marked.asksDeathReplacementsFirst(resolved);
    }

    @Override
    public boolean asksDeathReplacementsFirst(CardEffect effect) {
        return resolvesAsStep(((ResolveRiderForPlayerEffect) effect).rider());
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var step = (ResolveRiderForPlayerEffect) effect;
        CardEffect rider = step.rider();
        StackEntry riderEntry = new StackEntry(entry.getEntryType(), entry.getCard(), step.controllerId(),
                entry.getDescription(), List.of(rider), step.targetId(), entry.getSourcePermanentId());
        riderEntry.setEventValue(step.eventValue());
        riderEntry.getRemovedPermanentControllers().putAll(step.removedPermanentControllers());
        riderEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        riderEntry.setTriggeringPermanentId(entry.getTriggeringPermanentId());
        if (!step.targetCardIds().isEmpty()) {
            riderEntry.setTargetCardIds(step.targetCardIds());
        }

        if (rider instanceof ConditionalEffect conditional) {
            if (!conditionEvaluationService.isMet(
                    gameData, conditional.condition(), ConditionContext.forStackEntry(riderEntry))) {
                return;
            }
            rider = conditional.wrapped();
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(rider);
        if (handler == null) {
            log.warn("Game {} - No handler for rider: {}", gameData.id, rider.getClass().getSimpleName());
            return;
        }
        handler.resolve(gameData, riderEntry, rider);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
