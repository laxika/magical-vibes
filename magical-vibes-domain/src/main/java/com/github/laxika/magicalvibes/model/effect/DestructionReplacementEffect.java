package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/** A static effect that replaces destruction of a permanent. */
public interface DestructionReplacementEffect extends CardEffect {

    DestructionReplacement replacement();

    boolean appliesTo(Permanent source, Permanent destroyedPermanent);
}
