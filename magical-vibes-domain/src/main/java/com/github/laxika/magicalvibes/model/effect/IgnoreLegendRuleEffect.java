package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * STATIC: "the legend rule doesn't apply [to permanents]": the legend rule (CR 704.5j) leaves alone each permanent
 * {@link #permanents()} names, judged by the layered pass. The default is every permanent (Mirror Gallery); "to
 * permanents you control" is {@code permanentsYouControl()} (Mirror Box, Sakashima of a Thousand Faces), "to creatures
 * you control" {@code creaturesYouControl()} (Council of Reeds), "to Spiders / Slivers you control" and "to [creature]
 * tokens you control" are {@code controlledBy(you(), filter)} (Spider-Verse, Sliver Gravemother, Cadric, Soul Kindler,
 * The Master, Multiplied). A player who still controls two or more legendary permanents with the same name that are not
 * exempt chooses among those.
 *
 * @param permanents the permanents the legend rule doesn't apply to, a static set
 */
public record IgnoreLegendRuleEffect(PermanentSubject permanents) implements CardEffect {

    public IgnoreLegendRuleEffect {
        Objects.requireNonNull(permanents, "permanents");
        if (permanents.needsStackEntry()) {
            throw new IllegalArgumentException("A static ability names a static set: " + permanents);
        }
    }

    /** "The legend rule doesn't apply." */
    public IgnoreLegendRuleEffect() {
        this(Subjects.allPermanents());
    }
}
