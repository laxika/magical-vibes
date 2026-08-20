package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantActivatePlaneswalkerLoyaltyAbilitiesThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpponentsCantActivatePlaneswalkerLoyaltyAbilitiesThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentsCantActivatePlaneswalkerLoyaltyAbilitiesThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                gameData.playersCantActivatePlaneswalkerLoyaltyAbilitiesThisTurn.add(playerId);
            }
        }

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + "'s opponents can't activate planeswalkers' loyalty abilities this turn."));
    }
}
