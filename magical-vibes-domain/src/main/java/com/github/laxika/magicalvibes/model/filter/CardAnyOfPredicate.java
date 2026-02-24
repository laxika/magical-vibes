package com.github.laxika.magicalvibes.model.filter;

import java.util.List;

public record CardAnyOfPredicate(List<CardPredicate> predicates) implements CardPredicate {
}
