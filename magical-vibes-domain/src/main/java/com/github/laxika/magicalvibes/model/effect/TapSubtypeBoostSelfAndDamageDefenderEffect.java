package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Attack trigger: you may tap X untapped [subtype] you control.
 * If you do, this creature gets +X/+0 until end of turn and
 * deals X damage to the player or planeswalker it's attacking.
 */
public record TapSubtypeBoostSelfAndDamageDefenderEffect(CardSubtype subtype) implements CardEffect {
}
