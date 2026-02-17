package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static effect: this creature can't be blocked by creatures with the specified subtype.
 * For example, Juggernaut can't be blocked by Walls.
 */
public record CantBeBlockedBySubtypeEffect(CardSubtype subtype) implements CardEffect {
}
