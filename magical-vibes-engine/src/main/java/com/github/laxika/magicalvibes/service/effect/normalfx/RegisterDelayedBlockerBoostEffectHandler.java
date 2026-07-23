package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedBlockerBoost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedBlockerBoostEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedBlockerBoostEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedBlockerBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedBlockerBoostEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(
                new DelayedBlockerBoost(controllerId, e.power(), e.toughness(), entry.getCard()));
        log.info("Game {} - {} registers delayed blocker boost +{}/+{} for this turn",
                gameData.id, entry.getCard().getName(), e.power(), e.toughness());
    }
}
