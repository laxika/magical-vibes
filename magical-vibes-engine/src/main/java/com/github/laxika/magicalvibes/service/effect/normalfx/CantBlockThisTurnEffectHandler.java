package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantBlockThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CantBlockThisTurnEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(entry.getControllerId());
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry);
            case ENCHANTED -> resolveEnchanted(gameData, entry);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e, filterContext);
            case TARGET_CONTROLLERS_OTHER_CREATURES -> resolveTargetControllersOtherCreatures(gameData, entry, e, filterContext);
            case ALL_CREATURES -> resolveAllCreatures(gameData, e, filterContext);
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
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't block this turn."));
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

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't block this turn."));
        log.info("Game {} - {} can't block this turn", gameData.id, target.getCard().getName());
    }

    private void resolveEnchanted(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !source.isAttached()) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        if (enchanted == null) {
            return;
        }

        enchanted.setCantBlockThisTurn(true);
        gameLogService.append(gameData, GameLog.cardThen(enchanted.getCard(), " can't block this turn."));
        log.info("Game {} - {} can't block this turn", gameData.id, enchanted.getCard().getName());
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry, CantBlockThisTurnEffect e,
                                                FilterContext filterContext) {
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
                        || predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext))) {
                p.setCantBlockThisTurn(true);
                count++;
            }
        }

        if (count > 0) {
            String logEntry = "Creatures controlled by " + playerName + " can't block this turn.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} creatures controlled by {} can't block this turn", gameData.id, count, playerName);
        }
    }

    /**
     * "Other creatures that player controls can't block this turn" — the affected player is the
     * target permanent's controller, and the target itself is deliberately left able to block.
     */
    private void resolveTargetControllersOtherCreatures(GameData gameData, StackEntry entry, CantBlockThisTurnEffect e,
                                                        FilterContext filterContext) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        UUID affectedPlayerId = gameQueryService.findPermanentController(gameData, targetId);
        if (affectedPlayerId == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(affectedPlayerId);
        if (battlefield == null) return;

        String playerName = gameData.playerIdToName.get(affectedPlayerId);
        int count = 0;
        for (Permanent p : battlefield) {
            if (!p.getId().equals(targetId)
                    && gameQueryService.isCreature(gameData, p)
                    && (e.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext))) {
                p.setCantBlockThisTurn(true);
                count++;
            }
        }

        if (count > 0) {
            gameLogService.append(gameData, GameLog.text(
                    "Other creatures controlled by " + playerName + " can't block this turn."));
            log.info("Game {} - {} other creatures controlled by {} can't block this turn",
                    gameData.id, count, playerName);
        }
    }

    private void resolveAllCreatures(GameData gameData, CantBlockThisTurnEffect e, FilterContext filterContext) {
        int count = 0;
        for (UUID playerId : gameData.playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)
                        && (e.filter() == null
                            || predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext))) {
                    p.setCantBlockThisTurn(true);
                    count++;
                }
            }
        }

        if (count > 0) {
            String logEntry = "Some creatures can't block this turn.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} creatures can't block this turn", gameData.id, count);
        }
    }
}
