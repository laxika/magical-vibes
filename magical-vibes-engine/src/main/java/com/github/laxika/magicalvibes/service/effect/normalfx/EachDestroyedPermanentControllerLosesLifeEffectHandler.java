package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachDestroyedPermanentControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachDestroyedPermanentControllerLosesLifeEffect} from the per-destroyed-
 * permanent controller tally on the resolving stack entry. The players lose their life at once
 * (one {@link LifeSupport#applyLifeLosses} call), which is also this handler's only change, so a
 * Lich's Duel Mastery sacrifice those losses cause is asked about before any life is lost.
 */
@Component
@RequiredArgsConstructor
public class EachDestroyedPermanentControllerLosesLifeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachDestroyedPermanentControllerLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            int count = (int) entry.getEventPlayerIds().stream().filter(playerId::equals).count();
            if (count > 0) {
                losses.put(playerId, count);
            }
        }
        lifeSupport.applyLifeLosses(gameData, losses, entry.getCard().getName());
    }
}
