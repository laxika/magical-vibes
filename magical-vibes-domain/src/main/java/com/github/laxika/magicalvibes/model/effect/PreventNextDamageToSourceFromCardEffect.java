package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Prevents the next 1 damage a specific card would deal to this ability's source permanent. */
public record PreventNextDamageToSourceFromCardEffect(UUID damageSourceCardId) implements CardEffect {
}
