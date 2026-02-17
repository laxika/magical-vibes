package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

public record DrawAndLoseLifePerSubtypeEffect(CardSubtype subtype) implements CardEffect {
}
