package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerCantGainLifeThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerCantGainLifeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        // targetId is either a player or a planeswalker; the locked player is that player or the
        // planeswalker's controller.
        UUID playerId = gameData.playerIds.contains(targetId)
                ? targetId
                : gameQueryService.findPermanentController(gameData, targetId);
        if (playerId == null) return;

        gameData.playersWhoCantGainLifeThisTurn.add(playerId);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(playerId) + " can't gain life this turn."));
    }
}
