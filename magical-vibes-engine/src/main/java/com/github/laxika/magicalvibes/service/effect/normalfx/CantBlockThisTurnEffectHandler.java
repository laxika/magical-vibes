package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CantBlockThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantBlockThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CantBlockThisTurnEffect) effect;
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e);
            case ALL_CREATURES -> resolveAllCreatures(gameData, e);
            default -> throw new IllegalStateException("Unsupported can't-block scope: " + e.scope());
        }
    }

    private void resolveTarget(GameData gameData, StackEntry entry) {
        // Multi-target: apply to each valid target
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.getTargetIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue;
                }
                target.setCantBlockThisTurn(true);
                String logMsg = target.getCard().getName() + " can't block this turn.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
                log.info("Game {} - {} can't block this turn", gameData.id, target.getCard().getName());
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.setCantBlockThisTurn(true);

        String logEntry = target.getCard().getName() + " can't block this turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} can't block this turn", gameData.id, target.getCard().getName());
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry, CantBlockThisTurnEffect e) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        // Determine the affected player: if target is a player, use directly;
        // if target is a planeswalker, use its controller
        UUID affectedPlayerId;
        if (gameData.playerIds.contains(targetId)) {
            affectedPlayerId = targetId;
        } else {
            affectedPlayerId = gameQueryService.findPermanentController(gameData, targetId);
            if (affectedPlayerId == null) return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(affectedPlayerId);
        if (battlefield == null) return;

        String playerName = gameData.playerIdToName.get(affectedPlayerId);
        int count = 0;
        for (Permanent p : battlefield) {
            if (gameQueryService.isCreature(gameData, p)
                    && (e.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))) {
                p.setCantBlockThisTurn(true);
                count++;
            }
        }

        if (count > 0) {
            String logEntry = "Creatures controlled by " + playerName + " can't block this turn.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} creatures controlled by {} can't block this turn", gameData.id, count, playerName);
        }
    }

    private void resolveAllCreatures(GameData gameData, CantBlockThisTurnEffect e) {
        int count = 0;
        for (UUID playerId : gameData.playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)
                        && (e.filter() == null
                            || predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))) {
                    p.setCantBlockThisTurn(true);
                    count++;
                }
            }
        }

        if (count > 0) {
            String logEntry = "Some creatures can't block this turn.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} creatures can't block this turn", gameData.id, count);
        }
    }
}
