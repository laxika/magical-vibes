package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;

/**
 * Replacement behavior for a permanent that may become a copy as it is turned face up.
 * Additional slot effects are copy exceptions retained by the resulting copy.
 */
public record TurnFaceUpCopyEffect(PermanentPredicate filter,
                                   Map<EffectSlot, List<CardEffect>> additionalSlotEffects)
        implements ReplacementEffect {

    public TurnFaceUpCopyEffect(PermanentPredicate filter) {
        this(filter, Map.of());
    }
}
