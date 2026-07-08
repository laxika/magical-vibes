package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.DelayedUntapPermanents;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUntapPermanentsEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedUntapPermanentsEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedUntapPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedUntapPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(
                new DelayedUntapPermanents(controllerId, e.count(), e.filter(), entry.getCard()));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers delayed untap up to {} permanents at next end step",
                gameData.id, playerName, e.count());
    }
}
