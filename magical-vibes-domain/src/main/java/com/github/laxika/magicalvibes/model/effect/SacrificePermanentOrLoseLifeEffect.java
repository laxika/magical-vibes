package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The player this effect acts on (the stack entry's {@code targetId} — for a Curse upkeep trigger,
 * the enchanted player baked by {@code StepTriggerService}) sacrifices one permanent matching
 * {@code filter} of their choice. If they control no matching permanent, they lose {@code lifeLoss}
 * life instead.
 *
 * <p>The edict-with-punisher template: Cruel Reality ("that player sacrifices a creature or
 * planeswalker of their choice. If the player can't, they lose 5 life."). Not a spell target — the
 * acted-on player is supplied on the stack entry, so {@code targetSpec()} stays {@code NONE}.
 *
 * @param filter   which permanents are eligible to be sacrificed
 * @param lifeLoss life lost when the player controls no matching permanent
 */
public record SacrificePermanentOrLoseLifeEffect(PermanentPredicate filter, int lifeLoss) implements CardEffect {
}
