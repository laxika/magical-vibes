package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

public record AnimateSelfEffect(List<CardSubtype> grantedSubtypes) implements CardEffect {
}
