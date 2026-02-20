package com.github.laxika.magicalvibes.model.filter;

import java.util.List;

public record PermanentAllOfPredicate(List<PermanentPredicate> predicates) implements PermanentPredicate {
}
