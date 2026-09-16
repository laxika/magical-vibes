package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTargetPlayerHandRevealedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayWithTargetPlayerHandRevealedEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayWithTargetPlayerHandRevealedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        resolveForPlayer(gameData, entry.getTargetId());
    }

    private void resolveForPlayer(GameData gameData, UUID playerId) {
        if (playerId == null || !gameData.playerIds.contains(playerId)) return;
        gameData.playersWithHandRevealed.add(playerId);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(playerId) + " plays with their hand revealed for the rest of the game."));
    }
}
