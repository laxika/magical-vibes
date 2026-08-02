package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AjaniSteadfastEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllButOneDamageToControllerAndPlaneswalkersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AjaniSteadfastEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AjaniSteadfastEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        gameData.emblems.add(new Emblem(
                controllerId,
                List.of(new PreventAllButOneDamageToControllerAndPlaneswalkersEffect()),
                entry.getCard()));

        gameLogService.append(gameData, GameLog.text(
                playerName + " gets an emblem with \"If a source would deal damage to you or a planeswalker you control, prevent all but 1 of that damage.\"."));
        log.info("Game {} - {} gets Ajani Steadfast emblem", gameData.id, playerName);
    }
}
