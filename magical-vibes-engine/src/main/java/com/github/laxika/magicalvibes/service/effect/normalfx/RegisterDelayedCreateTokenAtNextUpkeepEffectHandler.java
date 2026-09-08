package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenAtNextUpkeepEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedCreateTokenAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCreateTokenAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedCreateTokenAtNextUpkeepEffect) effect;
        gameData.queueDelayedAction(new DelayedCreateTokenAtNextUpkeep(
                entry.getControllerId(), e.tokenEffect(), entry.getCard()));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - {} registers delayed token creation at next upkeep",
                gameData.id, playerName);
    }
}
