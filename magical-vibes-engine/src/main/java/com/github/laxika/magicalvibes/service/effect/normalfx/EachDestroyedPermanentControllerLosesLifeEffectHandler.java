package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachDestroyedPermanentControllerLosesLifeEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachDestroyedPermanentControllerLosesLifeEffect} from the per-destroyed-
 * permanent controller tally on the resolving stack entry.
 */
@Component
@RequiredArgsConstructor
public class EachDestroyedPermanentControllerLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachDestroyedPermanentControllerLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            int count = (int) entry.getEventPlayerIds().stream().filter(playerId::equals).count();
            if (count > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, count, entry.getCard().getName());
            }
        }
    }
}
