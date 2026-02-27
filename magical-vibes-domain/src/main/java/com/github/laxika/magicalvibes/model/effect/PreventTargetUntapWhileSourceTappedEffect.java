package com.github.laxika.magicalvibes.model.effect;

/**
 * Effect: "Target permanent doesn't untap during its controller's untap step
 * for as long as [source permanent] remains tapped."
 *
 * When resolved, adds the source permanent's ID to the target permanent's
 * {@code untapPreventedByPermanentIds} set. During each untap step, the lock
 * is checked: if the source permanent is no longer tapped or no longer on the
 * battlefield, the lock is removed and the target can untap normally.
 *
 * This effect does NOT declare {@code canTargetPermanent()} because it piggybacks
 * on the targeting from a companion effect (e.g. {@link TapTargetPermanentEffect}).
 */
public record PreventTargetUntapWhileSourceTappedEffect() implements CardEffect {
}
