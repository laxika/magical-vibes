package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Puts counters on a permanent. Handles the full counter-placement family:
 * <ul>
 *   <li>a non-null {@code predicate} → resolution-time choice among the controller's matching
 *       permanents (non-targeting);</li>
 *   <li>otherwise a targeting effect, resolving the per-effect target set by
 *       {@code EffectResolutionService} for multi-group spells (e.g. River Heralds' Boon), the
 *       full {@code targetIds} list for a single effect that targets several permanents, or the
 *       lone {@code targetId}.</li>
 * </ul>
 * The counter count is a {@code DynamicAmount} ({@code Fixed}, {@code XValue()}, …). Placement is
 * routed through {@link PermanentCounterSupport#placeCounterOnPermanent} so counter-type-specific
 * behaviour (+1/+1 triggers, -1/-1 prevention, lore/saga chapters) is preserved. When
 * {@code regenerateIfSurvives} is set, the target is regenerated after placement if it survives.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnTargetPermanentEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        // Predicate-based resolution: choose from controller's battlefield (non-targeting).
        if (e.predicate() != null) {
            permanentCounterSupport.resolveCounterOnOwnPermanent(gameData, entry,
                    e.counterType(), count, e.predicate());
            return;
        }

        // Multi-group spell (e.g. River Heralds' Boon): each effect uses its own target,
        // remapped onto entry.targetId by EffectResolutionService. A null target means the
        // optional target for this group wasn't chosen — do nothing.
        if (entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && entry.getCard().getSpellTargets().size() > 1) {
            if (entry.getTargetId() != null) {
                placeOnTarget(gameData, entry, entry.getTargetId(), e, count);
            }
            return;
        }

        // Single-group, multiple targets: apply to each valid target.
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.getTargetIds()) {
                placeOnTarget(gameData, entry, targetId, e, count);
            }
            return;
        }

        // Single-target fallback.
        if (entry.getTargetId() == null) {
            log.info("Game {} - Target no longer on battlefield, effect fizzles", gameData.id);
            return;
        }
        placeOnTarget(gameData, entry, entry.getTargetId(), e, count);
    }

    private void placeOnTarget(GameData gameData, StackEntry entry, UUID targetId,
                               PutCounterOnTargetPermanentEffect e, int count) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return; // Partially resolves — skip removed targets.
        }
        if (gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }
        if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, e.counterType(), count);

        if (e.regenerateIfSurvives()) {
            int effectiveToughness = gameQueryService.getEffectiveToughness(gameData, target);
            if (effectiveToughness >= 1) {
                target.setRegenerationShield(target.getRegenerationShield() + 1);
                gameBroadcastService.logAndBroadcast(gameData,
                        target.getCard().getName() + " gains a regeneration shield.");
                log.info("Game {} - {} gains a regeneration shield (toughness {})",
                        gameData.id, target.getCard().getName(), effectiveToughness);
            }
        }
    }
}
