package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Attaches matching Equipment to the first permanent created earlier by the same resolution.
 * The effect is intended to follow a token-creation effect in a {@link SequenceEffect}.
 *
 * @param equipmentFilter filter for the Equipment to attach
 */
public record AttachMatchingEquipmentToCreatedPermanentEffect(PermanentPredicate equipmentFilter)
        implements CardEffect {
}
