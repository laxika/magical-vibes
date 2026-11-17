package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
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
        if (Duration.PERMANENT.equals(e.duration())) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), controllerId, e, controllerId, Duration.PERMANENT));
            period = "for the rest of the game";
        } else if (Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(e.duration())) {
            if (entry.getSourcePermanentId() == null) {
                return;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(), controllerId,
                    new NoMaximumHandSizeEffect(), null, controllerId, null,
                    Duration.WHILE_SOURCE_ON_BATTLEFIELD, 0));
            period = "for as long as they control " + entry.getCard().getName();
        } else {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), controllerId, e, controllerId, Duration.UNTIL_YOUR_NEXT_TURN));
            period = "until their next turn";
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " has no maximum hand size " + period + "."));
        log.info("Game {} - {} granted no maximum hand size {}", gameData.id, playerName, period);
    }
}
