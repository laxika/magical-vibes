package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for a static replacement effect that makes matching permanents enter untapped.
 */
public interface PermanentsEnterUntappedEffect extends ReplacementEffect {

    PermanentPredicate filter();
}
