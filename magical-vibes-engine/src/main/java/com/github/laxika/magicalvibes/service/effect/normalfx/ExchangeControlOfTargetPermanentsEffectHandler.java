package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExchangeControlOfTargetPermanentsEffect} (Puca's Mischief). Reads the two targets
 * from {@code targetIds}: for cards that pin the first target to the ability's controller, {@code [0]}
 * is that controller's permanent and {@code [1]} the opponent's; for "two target creatures" wordings
 * either slot may belong to anyone. Re-checks target legality at resolution (CR 701.12a: if either
 * target has become illegal the exchange doesn't happen) and, if both are still legal and controlled
 * by different players (CR 701.12b), swaps
 * their controllers permanently by creating two layer-2 control effects via {@link CreatureControlService}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeControlOfTargetPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControlOfTargetPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExchangeControlOfTargetPermanentsEffect exchange = (ExchangeControlOfTargetPermanentsEffect) effect;

        // In source-mode (Conjured Currency) the ability's own permanent stands in for the first
        // target, so only a single target is declared — it may arrive as the lone targetId.
        List<UUID> targetIds = entry.getTargetIds();
        UUID controllerId = entry.getControllerId();
        Permanent ownTarget;
        UUID opponentTargetId;
        if (exchange.sourceIsFirstTarget()) {
            opponentTargetId = targetIds != null && !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
            if (opponentTargetId == null) {
                return;
            }
            ownTarget = resolveSourcePermanent(gameData, entry);
        } else {
            if (targetIds == null || targetIds.size() < 2) {
                return;
            }
            opponentTargetId = targetIds.get(1);
            ownTarget = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        }
        Permanent opponentTarget = gameQueryService.findPermanentById(gameData, opponentTargetId);
        if (ownTarget == null || opponentTarget == null) {
            logFizzle(gameData, entry);
            return;
        }

        UUID ownController = gameQueryService.findPermanentController(gameData, ownTarget.getId());
        UUID opponentController = gameQueryService.findPermanentController(gameData, opponentTarget.getId());
        if (ownController == null || opponentController == null) {
            logFizzle(gameData, entry);
            return;
        }

        // Re-check legality (CR 701.12a): both targets must still be matching permanents and must be
        // controlled by different players (CR 701.12b - if the same player controls both, the
        // exchange does nothing). Cards whose wording pins the first target to the ability's
        // controller ("target land you control and target land an opponent controls") additionally
        // require that split; source-mode exchanges (Conjured Currency) skip the first-target
        // predicate because the source permanent stands in for that half.
        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
        boolean controllersDiffer = !ownController.equals(opponentController);
        boolean ownershipSplitOk = !exchange.requireFirstTargetControlledByController()
                || (ownController.equals(controllerId) && !opponentController.equals(controllerId));
        boolean stillLegal = controllersDiffer
                && ownershipSplitOk
                && (exchange.sourceIsFirstTarget()
                        || predicateEvaluationService.matchesPermanentPredicate(ownTarget, exchange.targetPredicate(), filterContext))
                && predicateEvaluationService.matchesPermanentPredicate(opponentTarget, exchange.targetPredicate(), filterContext)
                && (!exchange.requireOpponentManaValueNotGreater()
                        || opponentTarget.getCard().getManaValue() <= ownTarget.getCard().getManaValue())
                && (!exchange.requireSharedArtifactOrCreatureType()
                        || gameQueryService.sharesArtifactOrCreatureType(ownTarget, opponentTarget));
        if (!stillLegal) {
            logFizzle(gameData, entry);
            return;
        }

        // Exchange: give the controller's permanent to the opponent, and the opponent's to the controller.
        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        creatureControlService.applyControlEffect(gameData, opponentController, ownTarget,
                controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null, entry.getCard().getName());
        creatureControlService.applyControlEffect(gameData, ownController, opponentTarget,
                controlEffect, ControlDuration.PERMANENT.toEffectDuration(), null, entry.getCard().getName());

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(": ").card(ownTarget.getCard()).text(" and ").card(opponentTarget.getCard()).text(" exchange controllers.").build());
        log.info("Game {} - {} exchanges control of {} and {}", gameData.id, entry.getCard().getName(),
                ownTarget.getCard().getName(), opponentTarget.getCard().getName());
    }

    /**
     * Finds the permanent the ability came from. Trigger paths that route through the may-ability
     * target selection drop {@code sourcePermanentId}, so fall back to matching the entry's card.
     */
    private Permanent resolveSourcePermanent(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            Permanent byId = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (byId != null) {
                return byId;
            }
        }
        if (entry.getCard() == null) {
            return null;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(entry.getCard().getId())) {
                    return permanent;
                }
            }
        }
        return null;
    }

    private void logFizzle(GameData gameData, StackEntry entry) {
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s exchange has no effect (a target is no longer legal)."));
        log.info("Game {} - {} exchange fizzles (illegal target)", gameData.id, entry.getCard().getName());
    }
}
