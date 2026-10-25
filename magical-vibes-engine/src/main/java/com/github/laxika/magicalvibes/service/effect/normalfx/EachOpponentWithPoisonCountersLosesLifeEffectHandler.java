package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.Map;
import java.util.LinkedHashMap;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentWithPoisonCountersLosesLifeEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves life loss limited to opponents meeting a poison-counter threshold. */
@Component
@RequiredArgsConstructor
public class EachOpponentWithPoisonCountersLosesLifeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentWithPoisonCountersLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentWithPoisonCountersLosesLifeEffect lifeLoss =
                (EachOpponentWithPoisonCountersLosesLifeEffect) effect;
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(entry.getControllerId())
                    && gameData.playerPoisonCounters.getOrDefault(playerId, 0)
                    >= lifeLoss.minimumPoisonCounters()) {
                losses.put(playerId, lifeLoss.amount());
            }
        }
        lifeSupport.applyLifeLosses(gameData, losses, entry.getCard().getName());
    }
}
