package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Static effect that grants a subtype to controlled lands and owned land cards outside the battlefield. */
public record GrantSubtypeToOwnLandsAndLandCardsEffect(CardSubtype subtype)
        implements OwnLandSubtypeGrantingEffect {
}
