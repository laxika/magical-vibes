package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * STATIC: "if there are exactly two [counted] permanents named ~, the legend rule doesn't apply to them": while
 * {@link #counted()} names exactly two permanents with the source's name, the legend rule (CR 704.5j) leaves those two
 * alone, the source's copy included or not. Brothers Yamazaki counts every permanent on the battlefield (the default),
 * Mothers Yamazaki "permanents you control" ({@code permanentsYouControl()}), Syr Joshua and Syr Saxon "legendary
 * permanents you control" ({@code controlledBy(you(), legendary)}); a third one anywhere in the set switches it off.
 *
 * @param counted the permanents counted for the source's name, a static set
 */
public record IgnoreLegendRuleWhenExactlyTwoSameNameEffect(PermanentSubject counted) implements CardEffect {

    public IgnoreLegendRuleWhenExactlyTwoSameNameEffect {
        Objects.requireNonNull(counted, "counted");
        if (counted.needsStackEntry()) {
            throw new IllegalArgumentException("A static ability names a static set: " + counted);
        }
    }

    /** "If there are exactly two permanents named ~ on the battlefield". */
    public IgnoreLegendRuleWhenExactlyTwoSameNameEffect() {
        this(Subjects.allPermanents());
    }
}
