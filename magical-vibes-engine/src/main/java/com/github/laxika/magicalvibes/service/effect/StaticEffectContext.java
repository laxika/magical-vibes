package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Inputs shared by static-effect handlers. The source controller is explicit because an entering
 * permanent can be evaluated before battlefield membership makes its controller discoverable.
 */
public record StaticEffectContext(Permanent source, Permanent target, UUID sourceControllerId,
                                  boolean targetOnSameBattlefield, GameData gameData,
                                  com.github.laxika.magicalvibes.model.planar.PlanarObject planarSource) {
    public StaticEffectContext(Permanent source, Permanent target, UUID sourceControllerId,
                               boolean targetOnSameBattlefield, GameData gameData) {
        this(source, target, sourceControllerId, targetOnSameBattlefield, gameData, null);
    }

    public UUID sourceId() { return planarSource != null ? planarSource.getId() : source.getId(); }

    public com.github.laxika.magicalvibes.model.Card sourceCard() {
        return planarSource != null ? planarSource.getCard() : source.getCard();
    }

    public int sourceCounterCount(com.github.laxika.magicalvibes.model.CounterType type) {
        return planarSource != null ? planarSource.getCounters().getOrDefault(type, 0) : source.getCounterCount(type);
    }

}

