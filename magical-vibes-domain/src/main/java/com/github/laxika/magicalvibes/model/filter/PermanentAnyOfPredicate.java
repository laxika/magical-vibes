package com.github.laxika.magicalvibes.model.filter;

import java.util.List;

public record PermanentAnyOfPredicate(List<PermanentPredicate> predicates) implements PermanentPredicate {
}
