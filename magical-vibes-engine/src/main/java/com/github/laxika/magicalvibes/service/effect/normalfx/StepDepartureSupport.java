package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
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
 * Sacrifices or destroys permanents as the resolving step ({@link DepartureService#departAsStep}) for the
 * "sacrifice it, then …" and "destroy it, then …" handlers, and runs what follows only once the permanents have
 * left (CR 608.2c): right away when the departure is done, or as the step inserted after this one when a CR 616.1
 * question ended the step. Logs, "whenever you sacrifice" triggers and orphaned Auras are handled by the departure
 * itself.
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
        departThenContinue(gameData, entry, step, sacrificeBatch(permanentId), continuation);
    }

    /** Departs {@code batch} (one event) as {@code step}; nothing follows it. */
    public void depart(GameData gameData, StackEntry entry, CardEffect step, DepartureBatch batch) {
        departureService.departAsStep(gameData, entry, step, batch);
    }

    /**
     * Destroys {@code permanents} as {@code step}, all as one event ({@code cantBeRegenerated} as the effect says);
     * nothing follows it. Does nothing when the list is empty.
     */
    public void destroy(GameData gameData, StackEntry entry, CardEffect step, List<Permanent> permanents,
                        boolean cantBeRegenerated) {
        List<DepartureIntent> intents = permanents.stream()
                .map(Permanent::getId)
                .distinct()
                .map(permanentId -> DepartureIntent.destroy(permanentId, cantBeRegenerated))
                .toList();
        if (intents.isEmpty()) {
            return;
        }
        departureService.departAsStep(gameData, entry, step, DepartureBatch.of(intents));
    }

    /**
     * Destroys {@code permanentId} as {@code step} ({@code cantBeRegenerated} as the effect says), then runs
     * {@code continuation}'s {@link ContinuesAfterDeparture#continueAfterDeparture} for it, whether or not it was
     * destroyed.
     */
    public void destroyThenContinue(GameData gameData, StackEntry entry, CardEffect step, UUID permanentId,
                                    boolean cantBeRegenerated, ContinuesAfterDeparture continuation) {
        departThenContinue(gameData, entry, step,
                DepartureBatch.of(List.of(DepartureIntent.destroy(permanentId, cantBeRegenerated))), continuation);
    }

    /**
     * Departs {@code batch} as {@code step}, then runs {@code continuation}'s
     * {@link ContinuesAfterDeparture#continueAfterDeparture}, which reads the departure from the scratchpad.
     */
    public void departThenContinue(GameData gameData, StackEntry entry, CardEffect step, DepartureBatch batch,
                                   ContinuesAfterDeparture continuation) {
        if (departureService.departAsStep(gameData, entry, step, batch) instanceof DepartureService.Outcome.Done) {
            continuation.continueAfterDeparture(gameData, entry, step);
        } else {
            insertAfterResolvingStep(entry, new ContinueAfterDepartureEffect(step));
        }
    }

    /**
     * "Sacrifice it. If you do, {@code payload}": sacrifices {@code permanentId} as {@code step} and resolves
     * {@code payload} against the same entry only if it was sacrificed. When {@code step} is the resolution step
     * itself, {@code payload} is always the next step, so a removal in it is its own step and can ask its CR 616.1
     * questions first (Blind Zealot's destroy); a nested dispatch resolves it at once.
     */
    public void sacrificeThenIfSacrificed(GameData gameData, StackEntry entry, CardEffect step, UUID permanentId,
                                          CardEffect payload) {
        boolean resolutionStep = step != null && gameData.stepDepartureEffect == step;
        if (departureService.departAsStep(gameData, entry, step, sacrificeBatch(permanentId))
                instanceof DepartureService.Outcome.Done && !resolutionStep) {
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
