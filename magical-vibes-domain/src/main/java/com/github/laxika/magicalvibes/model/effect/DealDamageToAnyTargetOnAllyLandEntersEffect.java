package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Emblem marker for damage to any target whenever a land of the given subtype enters under the
 * emblem controller's control.
 */
public record DealDamageToAnyTargetOnAllyLandEntersEffect(CardSubtype landSubtype, int damage)
        implements CardEffect {
}
