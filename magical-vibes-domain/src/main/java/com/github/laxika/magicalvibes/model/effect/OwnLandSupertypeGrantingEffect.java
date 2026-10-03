package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/** Capability for a static effect that grants a supertype to lands controlled by its source. */
public interface OwnLandSupertypeGrantingEffect extends CardEffect {

    CardSupertype supertype();
}
