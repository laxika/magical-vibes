package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry);
            case ALL_CREATURES -> resolveAllCreatures(gameData, e);
            default -> throw new IllegalStateException("Unsupported can't-attack scope: " + e.scope());
        }
    }

    private void resolveTarget(GameData gameData, StackEntry entry) {
        List<UUID> targetIds = entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()
                ? entry.getTargetIds()
                : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            target.setCantAttackThisTurn(true);
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't attack this turn."));
            log.info("Game {} - {} can't attack this turn", gameData.id, target.getCard().getName());
        }
    }

    private void resolveAllCreatures(GameData gameData, CantAttackThisTurnEffect e) {
        int count = 0;
        for (UUID playerId : gameData.playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)
                        && (e.filter() == null
                            || predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))) {
                    p.setCantAttackThisTurn(true);
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
