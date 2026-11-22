package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeRestOfGameEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlayersCantGainLifeRestOfGameEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersCantGainLifeRestOfGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var restriction = (PlayersCantGainLifeRestOfGameEffect) effect;
        resolveForPlayer(gameData, entry,
                subjectResolutionService.player(gameData, entry, effect, restriction.player()));
    }

    /** {@code playerId} can't gain life for the rest of the game. */
    public void resolveForPlayer(GameData gameData, StackEntry entry, UUID playerId) {
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), new PlayersCantGainLifeRestOfGameEffect(),
                playerId, Duration.PERMANENT));
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                + " can't gain life for the rest of the game."));
    }
}
