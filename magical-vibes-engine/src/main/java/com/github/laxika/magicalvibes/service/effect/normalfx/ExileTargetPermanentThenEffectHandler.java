package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the exile-plus-value family via {@link ExileTargetPermanentThenEffect}: exile the
 * targeted permanent, then resolve an existing then-effect through its own handler.
 *
 * <p>The controller (and owner) of the exiled permanent are snapshotted <em>before</em> exile. The
 * then-effect is then resolved against a derived stack entry whose controller is the chosen
 * {@link ThenEffectRecipient}, so the then-effect's ordinary {@code CONTROLLER}-style recipient lands
 * on the right player — without any then-effect needing its own "target permanent's controller"
 * variant. Mirrors {@link DestroyTargetPermanentThenEffectHandler} but exiles instead of destroying
 * (no regeneration / indestructible interaction).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameOutcomeService gameOutcomeService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetPermanentThenEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Snapshot the exiled permanent's controller / owner and evaluate the optional condition
        // (e.g. "if it was a Gideon planeswalker") BEFORE it leaves the battlefield.
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID targetOwnerId = gameData.defaultControllerOf(target.getId());
        boolean runThen = e.thenCondition() == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, target, e.thenCondition());

        permanentRemovalService.removePermanentToExile(gameData, target);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(target.getCard().getName() + " is exiled."));
        log.info("Game {} - {} is exiled by {}",
                gameData.id, target.getCard().getName(), entry.getCard().getName());
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (runThen) {
            UUID thenControllerId = switch (e.recipient()) {
                case TARGET_CONTROLLER -> targetControllerId;
                case TARGET_OWNER -> targetOwnerId;
                case CONTROLLER, TARGET_CONTROLLER_AS_TARGET, TARGET_OWNER_AS_TARGET -> entry.getControllerId();
            };
            if (thenControllerId != null) {
                UUID thenTargetId = switch (e.recipient()) {
                    case TARGET_CONTROLLER_AS_TARGET -> targetControllerId;
                    case TARGET_OWNER_AS_TARGET -> targetOwnerId;
                    default -> entry.getTargetId();
                };
                StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), thenControllerId,
                        entry.getDescription(), List.of(e.thenEffect()), thenTargetId, entry.getSourcePermanentId());
                thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

                EffectHandler handler = effectHandlerRegistry.getHandler(e.thenEffect());
                if (handler != null) {
                    handler.resolve(gameData, thenEntry, e.thenEffect());
                } else {
                    log.warn("Game {} - No handler for then-effect: {}", gameData.id, e.thenEffect().getClass().getSimpleName());
                }
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
