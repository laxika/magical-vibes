package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target permanent phases out." (Reality Ripple). The targeted permanent — plus anything attached
 * to it (CR 702.26g) — leaves the battlefield into {@code GameData.phasedOutPermanents} and is
 * removed from combat (CR 506.4). Because it phased out directly, it phases in during its
 * controller's next untap step even without the phasing keyword (CR 702.26a).
 *
 * <p>Targeted sibling of {@link PhaseOutSelfEffect}. Narrow the legal target with a
 * {@code PermanentPredicateTargetFilter} on the card (e.g. "artifact, creature, or land").
 */
public record PhaseOutTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
