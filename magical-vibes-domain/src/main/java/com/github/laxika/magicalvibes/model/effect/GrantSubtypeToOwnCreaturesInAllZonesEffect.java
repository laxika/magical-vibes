package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Static effect that gives a fixed creature subtype to other creatures you control and to creature cards you own outside the battlefield. */
public record GrantSubtypeToOwnCreaturesInAllZonesEffect(CardSubtype subtype)
        implements OwnCreatureSubtypeGrantingEffect {
}
