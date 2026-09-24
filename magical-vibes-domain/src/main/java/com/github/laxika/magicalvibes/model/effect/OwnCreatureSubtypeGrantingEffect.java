package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Capability for a static effect that grants a fixed creature subtype to owned creature cards outside the battlefield. */
public interface OwnCreatureSubtypeGrantingEffect extends CardEffect {

    CardSubtype subtype();
}
