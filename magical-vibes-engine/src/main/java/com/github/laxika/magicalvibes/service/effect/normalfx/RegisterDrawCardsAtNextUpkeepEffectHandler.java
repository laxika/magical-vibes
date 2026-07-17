package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDrawCardsAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDrawCardsAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDrawCardsAtNextUpkeepEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(new DrawCardsAtNextUpkeep(controllerId, e.count(), entry.getCard()));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers delayed draw of {} at next upkeep", gameData.id, playerName, e.count());
    }
}
