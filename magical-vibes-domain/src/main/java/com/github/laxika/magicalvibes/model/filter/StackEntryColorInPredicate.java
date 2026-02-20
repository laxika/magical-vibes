package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

public record StackEntryColorInPredicate(Set<CardColor> colors) implements StackEntryPredicate {
}
