package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.TransformSourceAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterTransformSourceAtNextUpkeepEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterTransformSourceAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterTransformSourceAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        gameData.queueDelayedAction(new TransformSourceAtNextUpkeep(
                entry.getSourcePermanentId(), entry.getControllerId(), entry.getCard()));

        log.info("Game {} - {} registers delayed transform at next upkeep",
                gameData.id, entry.getCard().getName());
    }
}
