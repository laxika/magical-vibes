package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
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
public class CantAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CantAttackThisTurnEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(entry.getControllerId());
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry, e);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e, filterContext);
            case ALL_CREATURES -> resolveAllCreatures(gameData, entry, e, filterContext);
            default -> throw new IllegalStateException("Unsupported can't-attack scope: " + e.scope());
        }
    }

    private void resolveTarget(GameData gameData, StackEntry entry, CantAttackThisTurnEffect e) {
        List<UUID> targetIds = entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()
                ? entry.getTargetIds()
                : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            applyRestriction(gameData, entry, target, e);
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't attack this turn."));
            log.info("Game {} - {} can't attack this turn", gameData.id, target.getCard().getName());
        }
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry,
                                                 CantAttackThisTurnEffect e, FilterContext filterContext) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetId);
        if (battlefield == null) return;

        String playerName = gameData.playerIdToName.get(targetId);
        int count = 0;
        for (Permanent p : battlefield) {
            if (gameQueryService.isCreature(gameData, p)
                    && (e.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext))) {
                applyRestriction(gameData, entry, p, e);
                count++;
            }
        }

        if (count > 0) {
            gameLogService.append(gameData, GameLog.text(
                    "Creatures controlled by " + playerName + " can't attack this turn."));
            log.info("Game {} - {} creatures controlled by {} can't attack this turn",
                    gameData.id, count, playerName);
        }
    }

    private void applyRestriction(GameData gameData, StackEntry entry, Permanent permanent, CantAttackThisTurnEffect effect) {
        if (effect.duration() == com.github.laxika.magicalvibes.model.effect.GrantDuration.UNTIL_YOUR_NEXT_TURN) {
            gameData.permanentsCantAttackUntilNextTurn.computeIfAbsent(permanent.getId(),
                    ignored -> new java.util.HashSet<>()).add(entry.getControllerId());
        } else if (effect.duration() == com.github.laxika.magicalvibes.model.effect.GrantDuration.END_OF_TURN) {
            permanent.setCantAttackThisTurn(true);
        } else {
            throw new IllegalArgumentException("Unsupported attack restriction duration");
        }
    }

    private void resolveAllCreatures(GameData gameData, StackEntry entry, CantAttackThisTurnEffect e, FilterContext filterContext) {
        int count = 0;
        for (UUID playerId : gameData.playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)
                        && (e.filter() == null
                            || predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext))) {
                    applyRestriction(gameData, entry, p, e);
                    count++;
                }
            }
        }

        if (count > 0) {
            gameLogService.append(gameData, GameLog.text("Some creatures can't attack this turn."));
            log.info("Game {} - {} creatures can't attack this turn", gameData.id, count);
        }
    }
}
