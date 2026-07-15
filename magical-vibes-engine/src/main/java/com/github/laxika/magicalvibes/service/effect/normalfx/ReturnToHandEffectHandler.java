package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Unified handler for {@link ReturnToHandEffect}. Dispatches on the effect's
 * {@link com.github.laxika.magicalvibes.model.effect.BounceScope}: {@code TARGET} (the chosen target
 * permanent(s), with the optional controller-life-loss rider), {@code SELF} (the source),
 * {@code SELF_SPELL} (the resolving spell returns itself to hand off the stack),
 * {@code ALL_MATCHING} (every permanent matching the filter across all battlefields),
 * {@code TARGET_PLAYERS_PERMANENTS} (the target player's matching permanents), and
 * {@code TARGET_PLAYERS_OWNED} (permanents the target player owns, any controller).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BounceSupport bounceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnToHandEffect) effect;
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry, e);
            case SELF -> bounceSupport.applyReturnSelfToHand(gameData, entry);
            case SELF_SPELL -> entry.setReturnToHandAfterResolving(true);
            case ALL_MATCHING -> resolveAllMatching(gameData, entry, e);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e);
            case TARGET_PLAYERS_OWNED -> resolveTargetPlayersOwned(gameData, entry, e);
        }
    }

    private void resolveTarget(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        List<UUID> targetIds = entry.getTargetIds().isEmpty()
                ? List.of(entry.getTargetId())
                : entry.getTargetIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            UUID controllerId = e.lifeLoss() > 0
                    ? gameQueryService.findPermanentController(gameData, target.getId())
                    : null;

            if (permanentRemovalService.removePermanentToHand(gameData, target)) {
                String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
            }

            if (controllerId != null) {
                if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(controllerId) + "'s life total can't change."));
                } else {
                    int currentLife = gameData.getLife(controllerId);
                    gameData.playerLifeTotals.put(controllerId, currentLife - e.lifeLoss());

                    String playerName = gameData.playerIdToName.get(controllerId);
                    String lifeLog = playerName + " loses " + e.lifeLoss() + " life (" + entry.getCard().getName() + ").";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(lifeLog));
                    log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, e.lifeLoss(), entry.getCard().getName());
                }
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        if (e.lifeLoss() > 0) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    private void resolveAllMatching(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                toReturn.addAll(battlefield.stream()
                        .filter(p -> e.filter() == null
                                || predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext))
                        .toList()));

        bounceAll(gameData, entry, toReturn);
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> toReturn = battlefield.stream()
                .filter(p -> e.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                .toList();

        bounceAll(gameData, entry, toReturn);
    }

    private void resolveTargetPlayersOwned(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((controllingPlayerId, battlefield) ->
                toReturn.addAll(battlefield.stream()
                        .filter(p -> {
                            UUID ownerId = gameData.stolenCreatures.getOrDefault(p.getId(), controllingPlayerId);
                            return ownerId.equals(targetPlayerId);
                        })
                        .filter(p -> e.filter() == null
                                || predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))
                        .toList()));

        bounceAll(gameData, entry, toReturn);
    }

    private void bounceAll(GameData gameData, StackEntry entry, List<Permanent> toReturn) {
        for (Permanent permanent : toReturn) {
            permanentRemovalService.removePermanentToHand(gameData, permanent);

            String logEntry = permanent.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, permanent.getCard().getName(), entry.getCard().getName());
        }

        if (!toReturn.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }
}
