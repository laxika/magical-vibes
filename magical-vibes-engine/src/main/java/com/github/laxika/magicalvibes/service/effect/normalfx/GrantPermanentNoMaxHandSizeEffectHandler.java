package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPermanentNoMaxHandSizeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantPermanentNoMaxHandSizeEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantPermanentNoMaxHandSizeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        gameData.playersWithNoMaximumHandSize.add(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " has no maximum hand size for the rest of the game."));
        log.info("Game {} - {} granted permanent no maximum hand size", gameData.id, playerName);
    
    }
}
