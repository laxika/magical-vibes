package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetFilter;

import java.util.Set;

public record SpellTypeTargetFilter(Set<StackEntryType> spellTypes) implements TargetFilter {
}
