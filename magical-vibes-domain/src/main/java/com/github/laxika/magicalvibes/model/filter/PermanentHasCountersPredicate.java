package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

public record PermanentHasCountersPredicate(CounterType counterType) implements PermanentPredicate {
}
