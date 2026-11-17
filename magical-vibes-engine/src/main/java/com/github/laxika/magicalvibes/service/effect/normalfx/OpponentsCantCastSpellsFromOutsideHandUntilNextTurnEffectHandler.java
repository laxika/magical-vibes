package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsFromOutsideHandUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class OpponentsCantCastSpellsFromOutsideHandUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentsCantCastSpellsFromOutsideHandUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.playerIds) {
            if (!playerId.equals(controllerId)) {
                gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                        entry.getSourcePermanentId(), controllerId, effect, playerId,
                        Duration.UNTIL_YOUR_NEXT_TURN));
            }
        }
        gameLogService.append(gameData, GameLog.text(
                "Opponents can't cast spells from anywhere other than their hands until your next turn."));
    }
}
