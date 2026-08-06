package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantNoMaximumHandSizeEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantNoMaximumHandSizeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantNoMaximumHandSizeEffect) effect;
        UUID controllerId = entry.getControllerId();

        String period;
        if (e.duration() == NoMaximumHandSizeDuration.REST_OF_GAME) {
            gameData.playersWithNoMaximumHandSize.add(controllerId);
            period = "for the rest of the game";
        } else {
            gameData.playersWithNoMaximumHandSizeUntilNextTurn.add(controllerId);
            period = "until their next turn";
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " has no maximum hand size " + period + "."));
        log.info("Game {} - {} granted no maximum hand size {}", gameData.id, playerName, period);
    }
}
