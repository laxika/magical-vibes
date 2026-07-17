package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreventNextDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var prevent = (PreventNextDamageToControllerEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;

        int currentShield = gameData.playerDamagePreventionShields.getOrDefault(controllerId, 0);
        gameData.playerDamagePreventionShields.put(controllerId, currentShield + prevent.amount());

        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "The next " + prevent.amount() + " damage that would be dealt to " + controllerName + " is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - Prevention shield {} added to controller {}", gameData.id, prevent.amount(), controllerName);
    }
}
