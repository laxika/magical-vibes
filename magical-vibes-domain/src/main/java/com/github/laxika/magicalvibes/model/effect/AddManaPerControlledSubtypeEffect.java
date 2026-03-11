package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

public record AddManaPerControlledSubtypeEffect(ManaColor color, CardSubtype subtype) implements ManaProducingEffect {
}
