package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static evasion restriction on attackers.
 * This creature can be blocked only by creatures with flying or creatures with the given subtype.
 */
public record CanBeBlockedOnlyByFlyingOrSubtypeEffect(CardSubtype subtype) implements CardEffect {
}
