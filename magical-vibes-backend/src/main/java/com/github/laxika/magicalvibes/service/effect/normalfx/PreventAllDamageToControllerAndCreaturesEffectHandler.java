package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventAllDamageToControllerAndCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventAllDamageToControllerAndCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.playersWithAllDamagePrevented.add(controllerId);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "All damage that would be dealt to " + playerName + " and creatures " + playerName + " controls this turn is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }
}
