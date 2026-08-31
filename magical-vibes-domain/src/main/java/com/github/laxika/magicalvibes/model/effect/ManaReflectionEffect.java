package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Static replacement effect that multiplies mana produced by a matching permanent. The no-arg
 * form is used by Mana Reflection and matches every permanent at two times; a predicate and
 * multiplier narrow and scale the affected mana source for cards such as Virtue of Strength.
 * Multiple instances stack multiplicatively.
 */
public record ManaReflectionEffect(PermanentPredicate permanentFilter, int multiplier) implements CardEffect {

    public ManaReflectionEffect() {
        this(new PermanentTruePredicate(), 2);
    }

    public ManaReflectionEffect(PermanentPredicate permanentFilter) {
        this(permanentFilter, 2);
    }
}
