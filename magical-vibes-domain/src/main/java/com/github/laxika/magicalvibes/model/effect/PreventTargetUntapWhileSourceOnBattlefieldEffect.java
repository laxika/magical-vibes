package com.github.laxika.magicalvibes.model.effect;

/**
 * Effect: "Target permanent doesn't untap during its controller's untap step
 * for as long as you control [source permanent]."
 *
 * When resolved, adds the source permanent's ID to the target permanent's
 * {@code untapPreventedWhileSourceOnBattlefieldIds} set. During each untap step,
 * the lock is checked: if the source permanent is no longer on the battlefield,
 * the lock is removed and the target can untap normally.
 *
 * Unlike {@link PreventTargetUntapWhileSourceTappedEffect}, this variant does NOT
 * require the source to remain tapped — only that it remains on the battlefield.
 * This is used by permanents like Time of Ice where the "doesn't untap" condition
 * is tied to the source being controlled, not to its tapped state.
 *
 * This effect does NOT declare {@code canTargetPermanent()} because it piggybacks
 * on the targeting from a companion effect (e.g. {@link TapTargetPermanentEffect}).
 */
public record PreventTargetUntapWhileSourceOnBattlefieldEffect() implements CardEffect {
}
