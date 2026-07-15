package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.VenserEmblemEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VenserEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return VenserEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        Emblem emblem = new Emblem(controllerId, List.of(
                new ExileTargetOnControllerSpellCastEffect()
        ), entry.getCard());

        gameData.emblems.add(emblem);

        String logEntry = playerName + " gets an emblem with \"Whenever you cast a spell, exile target permanent.\".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gets Venser emblem", gameData.id, playerName);
    }
}
