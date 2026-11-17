package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpponentsCantGainLifeThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentsCantGainLifeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;

        for (UUID playerId : gameData.playerIds) {
            if (!playerId.equals(controllerId)) {
                gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                        entry.getSourcePermanentId(), controllerId, effect, playerId, Duration.UNTIL_END_OF_TURN));
            }
        }
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": opponents can't gain life this turn."));
    }
}
