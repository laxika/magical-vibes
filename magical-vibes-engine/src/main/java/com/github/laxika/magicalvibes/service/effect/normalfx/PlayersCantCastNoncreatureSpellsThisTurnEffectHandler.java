package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastNoncreatureSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayersCantCastNoncreatureSpellsThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersCantCastNoncreatureSpellsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PlayersCantCastNoncreatureSpellsThisTurnEffect restriction =
                (PlayersCantCastNoncreatureSpellsThisTurnEffect) effect;
        if (restriction.opponentsOnly()) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(entry.getControllerId())) {
                    gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                            entry.getSourcePermanentId(), entry.getControllerId(), effect, playerId,
                            Duration.UNTIL_END_OF_TURN));
                }
            }
        } else {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), effect, null,
                    Duration.UNTIL_END_OF_TURN));
        }
        String logEntry = restriction.opponentsOnly()
                ? gameData.playerIdToName.get(entry.getControllerId())
                + "'s opponents can't cast noncreature spells this turn."
                : "Players can't cast noncreature spells this turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
    }
}
