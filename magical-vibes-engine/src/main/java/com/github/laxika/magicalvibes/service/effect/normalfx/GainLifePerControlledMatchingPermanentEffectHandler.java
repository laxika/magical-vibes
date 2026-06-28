package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerControlledMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GainLifePerControlledMatchingPermanentEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifePerControlledMatchingPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainLifePerControlledMatchingPermanentEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int totalCount = 0;
        if (battlefield != null) {
            for (PermanentPredicate predicate : e.predicates()) {
                for (Permanent permanent : battlefield) {
                    if (gameQueryService.matchesPermanentPredicate(gameData, permanent, predicate)) {
                        totalCount++;
                    }
                }
            }
        }
        if (totalCount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " gains no life (no matching permanents controlled).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        lifeSupport.applyGainLife(gameData, controllerId, totalCount);
    }
}
