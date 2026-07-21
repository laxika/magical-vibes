package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves bounce-plus-value via {@link ReturnTargetPermanentToHandThenEffect}: return the targeted
 * permanent to its owner's hand, then (optionally gated by a post-bounce condition checked against
 * the original caster) resolve an existing then-effect through {@link EffectResolutionService}.
 *
 * <p>Controller and owner are snapshotted <em>before</em> the bounce. Mirrors
 * {@link ExileTargetPermanentThenEffectHandler}.
 */
@Slf4j
@Component
public class ReturnTargetPermanentToHandThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final EffectResolutionService effectResolutionService;
    private final GameOutcomeService gameOutcomeService;

    public ReturnTargetPermanentToHandThenEffectHandler(
            GameQueryService gameQueryService,
            PermanentRemovalService permanentRemovalService,
            GameBroadcastService gameBroadcastService,
            ConditionEvaluationService conditionEvaluationService,
            // @Lazy breaks EffectResolutionService → registry → this → EffectResolutionService.
            @Lazy EffectResolutionService effectResolutionService,
            GameOutcomeService gameOutcomeService) {
        this.gameQueryService = gameQueryService;
        this.permanentRemovalService = permanentRemovalService;
        this.gameBroadcastService = gameBroadcastService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.effectResolutionService = effectResolutionService;
        this.gameOutcomeService = gameOutcomeService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentToHandThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetPermanentToHandThenEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID targetOwnerId = gameData.defaultControllerOf(target.getId());

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}",
                    gameData.id, target.getCard().getName(), entry.getCard().getName());
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Post-bounce condition uses the ORIGINAL entry (caster), so "if you control a Zombie"
        // checks the caster after the bounce — not the then-effect's remapped controller.
        if (e.thenCondition() != null
                && !conditionEvaluationService.isMet(gameData, e.thenCondition(),
                ConditionContext.forStackEntry(entry))) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(),
                    "'s " + e.thenCondition().conditionName() + " ability does nothing ("
                            + e.thenCondition().conditionNotMetReason() + ")."));
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

        UUID thenTargetId = switch (e.recipient()) {
            case TARGET_CONTROLLER_AS_TARGET -> targetControllerId;
            case TARGET_OWNER_AS_TARGET -> targetOwnerId;
            default -> entry.getTargetId();
        };
        StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), thenControllerId,
                entry.getDescription(), List.of(e.thenEffect()), thenTargetId, entry.getSourcePermanentId());
        thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        effectResolutionService.resolveEffects(gameData, thenEntry);

        gameOutcomeService.checkWinCondition(gameData);
    }
}
