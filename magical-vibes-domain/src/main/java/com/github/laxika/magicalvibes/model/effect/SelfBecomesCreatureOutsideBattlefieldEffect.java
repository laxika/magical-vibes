package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Characteristic-defining marker for a card that is a creature with the given base power,
 * toughness, and subtypes in every zone except the battlefield. The layered battlefield pass has
 * no non-battlefield objects to mutate; card characteristic queries consume this marker directly.
 */
public record SelfBecomesCreatureOutsideBattlefieldEffect(
        int power,
        int toughness,
        List<CardSubtype> grantedSubtypes
) implements SelfAllZoneSubtypeGrantingEffect {

    public SelfBecomesCreatureOutsideBattlefieldEffect {
        grantedSubtypes = List.copyOf(grantedSubtypes);
    }
}
