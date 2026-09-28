package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersGainKeywordUntilEndOfTurnEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayersGainKeywordUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersGainKeywordUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PlayersGainKeywordUntilEndOfTurnEffect) effect;
        for (UUID playerId : gameData.playerIds) {
            gameData.playerKeywordsUntilEndOfTurn
                    .computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(grant.keyword());
        }
    }
}
