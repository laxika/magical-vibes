package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TargetFilter;

import java.util.Set;

public record SpellColorTargetFilter(Set<CardColor> colors) implements TargetFilter {
}
