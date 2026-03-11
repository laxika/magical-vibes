package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

public record CantBeTargetedByNonColorSourcesEffect(CardColor allowedColor) implements CardEffect {
}
