package com.github.laxika.magicalvibes.model.filter;

import java.util.List;

public record CardAllOfPredicate(List<CardPredicate> predicates) implements CardPredicate {
}
