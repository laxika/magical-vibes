package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import java.util.List;

/** Trigger descriptor for "whenever you play a land from exile" abilities. */
public record LandPlayFromExileTriggerEffect(List<CardEffect> resolvedEffects, TargetFilter targetFilter)
        implements CardEffect {

    public LandPlayFromExileTriggerEffect(List<CardEffect> resolvedEffects) {
        this(resolvedEffects, null);
    }
}
