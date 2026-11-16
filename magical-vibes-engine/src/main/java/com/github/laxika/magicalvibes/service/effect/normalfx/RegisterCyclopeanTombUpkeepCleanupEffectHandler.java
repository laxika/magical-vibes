package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterCyclopeanTombUpkeepCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllMireCountersFromChosenLandEffect;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Creates Cyclopean Tomb's delayed triggered ability from its last-known battlefield record: "at the beginning of each
 * of your upkeeps for the rest of the game, remove all mire counters from a land ...", its controller the leaves
 * ability's (CR 603.7e), triggering at every one of their upkeeps (CR 603.7b).
 */
@Component
public class RegisterCyclopeanTombUpkeepCleanupEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterCyclopeanTombUpkeepCleanupEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentSnapshot();
        if (source == null) {
            return;
        }

        var cleanup = new RemoveAllMireCountersFromChosenLandEffect(
                UUID.randomUUID(), Set.copyOf(source.getMireCounterLandIds()), Set.of());
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atEveryStep(
                entry.getControllerId(), entry.getCard(), cleanup.atEachOfYourUpkeeps(), source.getId(), null,
                DelayedReferents.NONE, null));
    }
}
