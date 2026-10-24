package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ContinueAfterDepartureEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Sacrifices a permanent as the resolving step ({@link DepartureService#departAsStep}) for the "sacrifice it,
 * then …" handlers, and runs what follows the sacrifice only once the permanent has left (CR 608.2c): right
 * away when the departure is done, or as the step inserted after this one when a CR 616.1 question ended the
 * step. Logs, "whenever you sacrifice" triggers and orphaned Auras are handled by the departure itself.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StepDepartureSupport {

    private final DepartureService departureService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    /** Sacrifices {@code permanentId} as {@code step}; nothing follows it. */
    public void sacrifice(GameData gameData, StackEntry entry, CardEffect step, UUID permanentId) {
        departureService.departAsStep(gameData, entry, step, sacrificeBatch(permanentId));
    }

    /**
     * Sacrifices {@code permanentId} as {@code step}, then runs {@code continuation}'s
     * {@link ContinuesAfterDeparture#continueAfterDeparture} for it.
     */
    public void sacrificeThenContinue(GameData gameData, StackEntry entry, CardEffect step, UUID permanentId,
                                      ContinuesAfterDeparture continuation) {
        if (departureService.departAsStep(gameData, entry, step, sacrificeBatch(permanentId))
                instanceof DepartureService.Outcome.Done) {
            continuation.continueAfterDeparture(gameData, entry, step);
        } else {
            insertAfterResolvingStep(entry, new ContinueAfterDepartureEffect(step));
        }
    }

    /**
     * "Sacrifice it. If you do, {@code payload}": sacrifices {@code permanentId} as {@code step} and resolves
     * {@code payload} against the same entry only if it was sacrificed.
     */
    public void sacrificeThenIfSacrificed(GameData gameData, StackEntry entry, CardEffect step, UUID permanentId,
                                          CardEffect payload) {
        if (departureService.departAsStep(gameData, entry, step, sacrificeBatch(permanentId))
                instanceof DepartureService.Outcome.Done) {
            if (entry.getResolutionResults().anySacrificed()) {
                dispatch(gameData, entry, payload);
            }
        } else {
            insertAfterResolvingStep(entry, ConditionalEffect.unless(PermanentLeftThisWay.sacrificed(), payload));
        }
    }

    /**
     * Resolves {@code payload} through its own handler against {@code entry}. {@link SequenceEffect} has no handler
     * of its own, so a multi-step payload is expanded here; dispatch is synchronous, so its steps must be too.
     */
    public void dispatch(GameData gameData, StackEntry entry, CardEffect payload) {
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
            log.warn("No handler for the effect after a sacrifice: {}", payload.getClass().getSimpleName());
        }
    }

    private static DepartureBatch sacrificeBatch(UUID permanentId) {
        return DepartureBatch.of(List.of(DepartureIntent.sacrifice(permanentId)));
    }

    /** A departure only ends the step of an effect the resolution loop is dispatching, so its index is set. */
    private static void insertAfterResolvingStep(StackEntry entry, CardEffect next) {
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(next));
    }
}
