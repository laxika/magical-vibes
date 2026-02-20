package com.github.laxika.magicalvibes.model.filter;

import java.util.List;

public record StackEntryAllOfPredicate(List<StackEntryPredicate> predicates) implements StackEntryPredicate {
}
