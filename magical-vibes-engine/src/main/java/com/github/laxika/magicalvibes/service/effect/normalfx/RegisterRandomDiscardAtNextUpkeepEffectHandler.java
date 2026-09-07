package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.RandomDiscardCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterRandomDiscardAtNextUpkeepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisterRandomDiscardAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterRandomDiscardAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterRandomDiscardAtNextUpkeepEffect) effect;
        gameData.queueDelayedAction(new RandomDiscardCardsAtNextUpkeep(
                entry.getControllerId(), e.count(), entry.getCard()));
        log.info("Game {} - {} registers delayed random discard of {} card(s) at next upkeep",
                gameData.id, entry.getCard().getName(), e.count());
    }
}
