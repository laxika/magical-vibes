package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Capability for a static effect that grants a subtype to lands controlled by its source. */
public interface OwnLandSubtypeGrantingEffect extends CardEffect {

    CardSubtype subtype();
}
