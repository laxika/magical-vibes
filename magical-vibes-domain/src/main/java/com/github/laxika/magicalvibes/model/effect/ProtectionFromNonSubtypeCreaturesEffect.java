package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Protection from creature sources that do not have the given subtype. */
public record ProtectionFromNonSubtypeCreaturesEffect(CardSubtype subtype) implements CardEffect {
}
