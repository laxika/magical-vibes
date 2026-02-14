package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TargetFilter;

import java.util.Set;

public record ReturnCreaturesToOwnersHandEffect(Set<TargetFilter> filters) implements CardEffect {
}
