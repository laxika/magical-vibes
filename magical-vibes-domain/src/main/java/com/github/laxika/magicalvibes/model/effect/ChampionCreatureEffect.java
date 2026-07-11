package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Champion a creature (or a creature of the specified subtype).
 * When this enters, sacrifice it unless you exile another matching creature you control.
 * When this leaves the battlefield, the exiled card returns to the battlefield.
 * {@code championedSubtype} null = any creature ("Champion a creature").
 */
public record ChampionCreatureEffect(CardSubtype championedSubtype) implements CardEffect {

    public ChampionCreatureEffect() {
        this(null);
    }
}
