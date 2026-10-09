package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RememberMireCounterLandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.HashSet;
import com.github.laxika.magicalvibes.model.action.CyclopeanTombUpkeepCleanup;
import java.util.UUID;

/** Records the land targeted by Cyclopean Tomb on the source permanent's last-known state. */
@Component
public class RememberMireCounterLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    public RememberMireCounterLandEffectHandler(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RememberMireCounterLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (source != null) {
            source.getMireCounterLandIds().addAll(targetIds);
        } else {
            if (entry.getSourcePermanentSnapshot() != null) {
                entry.getSourcePermanentSnapshot().getMireCounterLandIds().addAll(targetIds);
            }
            for (StackEntry pending : gameData.stack) {
                if (pending.getSourcePermanentSnapshot() != null
                        && pending.getSourcePermanentSnapshot().getId().equals(entry.getSourcePermanentId())) {
                    pending.getSourcePermanentSnapshot().getMireCounterLandIds().addAll(targetIds);
                }
            }
            synchronized (gameData.delayedActions) {
                for (int i = 0; i < gameData.delayedActions.size(); i++) {
                    if (gameData.delayedActions.get(i) instanceof CyclopeanTombUpkeepCleanup cleanup
                            && entry.getSourcePermanentId().equals(cleanup.sourcePermanentId())) {
                        var tracked = new HashSet<>(cleanup.trackedLandIds());
                        tracked.addAll(targetIds);
                        gameData.delayedActions.set(i, cleanup.withTrackedLandIds(tracked));
                    }
                }
            }
        }
    }
}
