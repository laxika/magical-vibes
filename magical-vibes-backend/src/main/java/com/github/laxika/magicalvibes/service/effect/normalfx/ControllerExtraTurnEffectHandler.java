package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerExtraTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControllerExtraTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ControllerExtraTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        for (int i = 0; i < e.count(); i++) {
            gameData.extraTurns.addFirst(controllerId);
        }

        String logEntry = playerName + " takes " + e.count() + " extra "
                + TurnSupport.pluralize("turn", e.count()) + " after this one.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} granted {} extra turn(s)", gameData.id, playerName, e.count());
    }
}
