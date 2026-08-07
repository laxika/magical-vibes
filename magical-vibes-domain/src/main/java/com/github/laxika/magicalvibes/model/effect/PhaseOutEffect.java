package com.github.laxika.magicalvibes.model.effect;

/**
 * "&lt;Permanent&gt; phases out." The permanent picked out by {@code subject} — plus anything
 * attached to it (CR 702.26g) — leaves the battlefield into {@code GameData.phasedOutPermanents}
 * and is removed from combat (CR 506.4). Because it phased out directly it phases in during its
 * controller's next untap step even without the phasing keyword (CR 702.26a).
 *
 * <p>One-shot. {@link PhaseOutSubject} decides which permanent and therefore whether the effect
 * targets; see that enum for the per-value contract.
 *
 * <p>Mass phasing is {@link PhaseOutPermanentsEffect} — a filter-driven battlefield scan rather
 * than a single named permanent.
 */
public record PhaseOutEffect(PhaseOutSubject subject) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return subject == PhaseOutSubject.TARGET
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
