package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TargetFilter;

import java.util.Set;

public record LandSubtypeTargetFilter(Set<CardSubtype> subtypes) implements TargetFilter {
}
