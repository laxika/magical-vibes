package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a creature an opponent controls would die, exile it instead."
 *
 * <p>Only replaces a creature moving from the battlefield to a graveyard (dying), and only for
 * creatures controlled by an opponent of this permanent's controller. Because the creature never
 * reaches a graveyard, its dies-triggers do not fire. Applied in {@code PermanentRemovalService},
 * which is the one place that knows both the dying permanent's controller and that it was a
 * creature. Used by Liesa, Forgotten Archangel.
 */
public record ExileOpponentCreaturesInsteadOfDyingEffect() implements CardEffect {
}
