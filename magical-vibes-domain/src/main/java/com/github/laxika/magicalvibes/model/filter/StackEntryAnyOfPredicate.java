package com.github.laxika.magicalvibes.model.filter;

import java.util.List;

public record StackEntryAnyOfPredicate(List<StackEntryPredicate> predicates) implements StackEntryPredicate {
}
