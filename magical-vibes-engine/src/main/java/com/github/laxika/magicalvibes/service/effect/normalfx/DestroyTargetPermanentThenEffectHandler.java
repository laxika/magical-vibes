package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the destroy-plus-value family via {@link DestroyTargetPermanentThenEffect}: destroy the
 * targeted permanent, then resolve an existing then-effect through its own handler.
 *
 * <p>The controller of the destroyed permanent and the requested last-known stat
 * ({@link com.github.laxika.magicalvibes.model.effect.EventStat}) are snapshotted <em>before</em>
 * destruction. The then-effect is then resolved against a derived stack entry whose controller is the
 * chosen {@link ThenEffectRecipient} and whose {@code eventValue} carries the snapshot, so the
 * then-effect's ordinary {@code CONTROLLER}-style recipient lands on the right player with the right
 * value — without any then-effect needing its own "target permanent's controller" variant.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentThenEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final ConditionEvaluationService conditionEvaluationService;
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
        UUID targetOwnerId = gameData.defaultControllerOf(target.getId());
        boolean wasLand = e.stat() == EventStat.BASIC_LAND_SEARCH_COUNT
                && predicateEvaluationService.matchesPermanentPredicate(
                        gameData, target, new PermanentIsLandPredicate());
        int statValue = switch (e.stat()) {
            case NONE -> 0;
            case MANA_VALUE -> target.getCard().getManaValue();
            case TOUGHNESS -> gameQueryService.getEffectiveToughness(gameData, target);
            case POWER -> gameQueryService.getPowerBasedDamage(gameData, target);
            case BASIC_LAND_SEARCH_COUNT -> 0;
        };
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId());
        boolean thenApplies = e.thenCondition() == null
                || predicateEvaluationService.matchesPermanentPredicate(
                        target, e.thenCondition(), filterContext);

        // Unless the card says "dies this way", the then-effect happens regardless of whether
        // destruction succeeds (indestructible / regeneration).
        boolean destroyed = destructionSupport.tryDestroyAndLog(
                gameData, target, entry.getCard().getName(), e.cannotBeRegenerated());
        if (e.stat() == EventStat.BASIC_LAND_SEARCH_COUNT) {
            statValue = wasLand && destroyed ? 2 : 1;
        }

        if (!thenApplies || (e.requiresDestruction() && !destroyed)) {
            return;
        }

        UUID thenControllerId = switch (e.recipient()) {
            case TARGET_CONTROLLER -> targetControllerId;
            case TARGET_OWNER -> targetOwnerId;
            case CONTROLLER, TARGET_CONTROLLER_AS_TARGET, TARGET_OWNER_AS_TARGET -> entry.getControllerId();
        };
        if (thenControllerId == null) {
            return;
        }

        // *_AS_TARGET retargets the rider at the destroyed permanent's controller/owner while the
        // caster stays the resolving controller (damage shields / "you control …" checks key on the source).
        UUID thenTargetId = switch (e.recipient()) {
            case TARGET_CONTROLLER_AS_TARGET -> targetControllerId;
            case TARGET_OWNER_AS_TARGET -> targetOwnerId;
            default -> entry.getTargetId();
        };
        CardEffect thenEffect = e.thenEffect();
        StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), thenControllerId,
                entry.getDescription(), List.of(thenEffect), thenTargetId, entry.getSourcePermanentId());
        thenEntry.setEventValue(statValue);
        thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        if (thenEffect instanceof ConditionalEffect conditional) {
            if (!conditionEvaluationService.isMet(
                    gameData, conditional.condition(), ConditionContext.forStackEntry(thenEntry))) {
                return;
            }
            thenEffect = conditional.wrapped();
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(thenEffect);
        if (handler != null) {
            handler.resolve(gameData, thenEntry, thenEffect);
        } else {
            log.warn("Game {} - No handler for then-effect: {}", gameData.id, thenEffect.getClass().getSimpleName());
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
