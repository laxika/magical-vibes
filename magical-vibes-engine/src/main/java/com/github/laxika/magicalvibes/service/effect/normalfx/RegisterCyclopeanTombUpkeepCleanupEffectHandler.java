package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.CyclopeanTombUpkeepCleanup;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterCyclopeanTombUpkeepCleanupEffect;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/** Converts Cyclopean Tomb's last-known battlefield record into a persistent delayed action. */
@Component
public class RegisterCyclopeanTombUpkeepCleanupEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterCyclopeanTombUpkeepCleanupEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentSnapshot();
        if (source == null || source.getMireCounterLandIds().isEmpty()) {
            return;
        }

        gameData.queueDelayedAction(new CyclopeanTombUpkeepCleanup(
                UUID.randomUUID(),
                entry.getCard(),
                entry.getControllerId(),
                Set.copyOf(source.getMireCounterLandIds()),
                Set.of()));
    }
}
