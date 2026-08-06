package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player mills two cards. If two cards that share a color were milled this way, the process
 * repeats. Milling fewer than two cards (an empty or single-card library) ends the process, as does
 * a pair with no colour in common — colorless cards never share a color.
 * <p>
 * Used by Grindstone. The loop always terminates because each iteration removes two cards from a
 * finite library.
 */
public record MillTwoRepeatIfSharedColorEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
