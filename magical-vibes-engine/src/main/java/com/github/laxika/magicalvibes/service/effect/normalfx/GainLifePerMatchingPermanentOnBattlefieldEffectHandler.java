package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerMatchingPermanentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GainLifePerMatchingPermanentOnBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifePerMatchingPermanentOnBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainLifePerMatchingPermanentOnBattlefieldEffect) effect;
        int totalCount = 0;
        for (PermanentPredicate predicate : e.predicates()) {
            int[] count = {0};
            gameData.forEachPermanent((playerId, permanent) -> {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, predicate)) {
                    count[0]++;
                }
            });
            totalCount += count[0];
        }
        if (totalCount == 0) {
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            String logEntry = playerName + " gains no life (no matching permanents on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), totalCount);
    }
}
