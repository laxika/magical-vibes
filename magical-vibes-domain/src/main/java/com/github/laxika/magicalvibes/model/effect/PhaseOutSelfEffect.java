package com.github.laxika.magicalvibes.model.effect;

/**
 * "This creature phases out." (Mist Dragon). One-shot, non-targeting: the source permanent (plus
 * anything attached to it, CR 702.26g) leaves the battlefield into
 * {@code GameData.phasedOutPermanents} and is removed from combat (CR 506.4). Because it phased
 * out directly, it phases in during its controller's next untap step even without the phasing
 * keyword (CR 702.26a).
 *
 * <p>Self-only sibling of {@link PhaseOutSelfAndCombatOpponentEffect}.
 */
public record PhaseOutSelfEffect() implements CardEffect {
}
