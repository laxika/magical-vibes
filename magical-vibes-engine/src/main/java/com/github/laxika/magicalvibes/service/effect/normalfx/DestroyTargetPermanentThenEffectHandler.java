package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
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
        int statValue = switch (e.stat()) {
            case NONE -> 0;
            case MANA_VALUE -> target.getCard().getManaValue();
            case TOUGHNESS -> gameQueryService.getEffectiveToughness(gameData, target);
        };
        boolean thenApplies = e.thenCondition() == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, target, e.thenCondition());

        // The then-effect happens regardless of whether destruction succeeds (indestructible / regeneration).
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), e.cannotBeRegenerated());

        if (!thenApplies) {
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
        StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), thenControllerId,
                entry.getDescription(), List.of(e.thenEffect()), thenTargetId, entry.getSourcePermanentId());
        thenEntry.setEventValue(statValue);
        thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        EffectHandler handler = effectHandlerRegistry.getHandler(e.thenEffect());
        if (handler != null) {
            handler.resolve(gameData, thenEntry, e.thenEffect());
        } else {
            log.warn("Game {} - No handler for then-effect: {}", gameData.id, e.thenEffect().getClass().getSimpleName());
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
