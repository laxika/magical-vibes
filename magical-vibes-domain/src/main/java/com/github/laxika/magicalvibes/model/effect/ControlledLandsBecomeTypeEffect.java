package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Global STATIC effect: every land the source's controller controls becomes the given basic land
 * type, losing its other land types and abilities and gaining that type's mana ability per rule
 * 305.7. Lands other players control are unaffected. Controller-scoped sibling of
 * {@link NonbasicLandsBecomeTypeEffect} (nonbasic-only, any controller) and
 * {@link LandsOfSubtypeBecomeTypeEffect} (subtype-keyed, any controller); handled by the same
 * layer-4 pass. Used by Celestial Dawn ("Lands you control are Plains").
 *
 * @param subtype the basic land type every controlled land becomes
 */
public record ControlledLandsBecomeTypeEffect(CardSubtype subtype) implements CardEffect {
}
