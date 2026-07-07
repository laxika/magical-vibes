package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.RiderRecipient;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the destroy-plus-value family via {@link DestroyTargetPermanentThenEffect}: destroy the
 * targeted permanent, then resolve an existing rider effect through its own handler.
 *
 * <p>The controller of the destroyed permanent and the requested last-known stat
 * ({@link com.github.laxika.magicalvibes.model.effect.EventStat}) are snapshotted <em>before</em>
 * destruction. The rider is then resolved against a derived stack entry whose controller is the
 * chosen {@link RiderRecipient} and whose {@code eventValue} carries the snapshot, so the rider's
 * ordinary {@code CONTROLLER}-style recipient lands on the right player with the right value —
 * without any rider effect needing its own "target permanent's controller" variant.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentThenEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetPermanentThenEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Snapshot everything read from the destroyed permanent BEFORE it leaves the battlefield.
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        int statValue = switch (e.stat()) {
            case NONE -> 0;
            case MANA_VALUE -> target.getCard().getManaValue();
            case TOUGHNESS -> gameQueryService.getEffectiveToughness(gameData, target);
        };
        boolean riderApplies = e.riderCondition() == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, target, e.riderCondition());

        // The rider happens regardless of whether destruction succeeds (indestructible / regeneration).
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName());

        if (!riderApplies) {
            return;
        }

        UUID riderControllerId = e.recipient() == RiderRecipient.TARGET_CONTROLLER
                ? targetControllerId
                : entry.getControllerId();
        if (riderControllerId == null) {
            return;
        }

        StackEntry riderEntry = new StackEntry(entry.getEntryType(), entry.getCard(), riderControllerId,
                entry.getDescription(), List.of(e.rider()), entry.getTargetId(), entry.getSourcePermanentId());
        riderEntry.setEventValue(statValue);
        riderEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        EffectHandler handler = effectHandlerRegistry.getHandler(e.rider());
        if (handler != null) {
            handler.resolve(gameData, riderEntry, e.rider());
        } else {
            log.warn("Game {} - No handler for rider effect: {}", gameData.id, e.rider().getClass().getSimpleName());
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
