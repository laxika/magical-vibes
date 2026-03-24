package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Forces the targeted player (from the stack entry's targetId) to sacrifice
 * a number of permanents matching the given filter. The targeted player chooses
 * which permanents to sacrifice.
 *
 * <p>Example: "target opponent sacrifices a permanent." →
 * {@code new TargetPlayerSacrificesPermanentsEffect(1, new PermanentTruePredicate())}
 *
 * @param count  number of permanents to sacrifice
 * @param filter which permanents are eligible (use {@code PermanentTruePredicate} for any permanent)
 */
public record TargetPlayerSacrificesPermanentsEffect(
        int count,
        PermanentPredicate filter
) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
