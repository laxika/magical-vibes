package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

public record AnimateSelfByChargeCountersEffect(List<CardSubtype> grantedSubtypes) implements CardEffect {
}
