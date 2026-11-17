package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpponentsCantCastSpellsThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentsCantCastSpellsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID pid : gameData.orderedPlayerIds) {
            if (!pid.equals(controllerId)) {
                gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                        entry.getSourcePermanentId(), entry.getControllerId(), effect, pid,
                        Duration.UNTIL_END_OF_TURN));
            }
        }

        String logEntry = gameData.playerIdToName.get(controllerId) + "'s opponents can't cast spells this turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
    }
}
