package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantGainLifeRestOfGameEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerCantGainLifeRestOfGameEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerCantGainLifeRestOfGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolveForPlayer(gameData, entry, entry.getTargetId());
    }

    /** {@code playerId} can't gain life for the rest of the game, by {@code entry}'s effect. */
    public void resolveForPlayer(GameData gameData, StackEntry entry, UUID playerId) {
        if (playerId == null || !gameData.playerIds.contains(playerId)) return;
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), new TargetPlayerCantGainLifeRestOfGameEffect(),
                playerId, Duration.PERMANENT));
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + " can't gain life for the rest of the game."));
    }
}
