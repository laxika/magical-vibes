package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that turns each noncreature artifact into an artifact creature
 * with power and toughness each equal to its mana value.
 * <p>
 * When {@code losesAllAbilities} is {@code true} the affected artifacts also lose all
 * abilities (layer 6), matching Titania's Song; March of the Machines uses {@code false}.
 *
 * @param losesAllAbilities whether animated artifacts also lose all abilities
 */
public record AnimateNoncreatureArtifactsEffect(boolean losesAllAbilities) implements CardEffect {

    /** Convenience constructor for the animate-only variant (March of the Machines). */
    public AnimateNoncreatureArtifactsEffect() {
        this(false);
    }
}
