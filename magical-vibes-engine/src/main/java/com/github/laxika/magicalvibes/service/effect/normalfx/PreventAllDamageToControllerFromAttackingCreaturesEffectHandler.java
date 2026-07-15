package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerFromAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventAllDamageToControllerFromAttackingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventAllDamageToControllerFromAttackingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.playersWithDamageFromAttackersPrevented.add(controllerId);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "All damage that would be dealt to " + playerName + " this turn by attacking creatures is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
    }
}
