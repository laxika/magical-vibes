package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "&lt;Permanent&gt; phases out", "all [permanents] you control phase out". The permanents {@code subject} names —
 * plus anything attached to them (CR 702.26g) — phase out together into {@code GameData.phasedOutPermanents} and are
 * removed from combat (CR 506.4). Because they phased out directly they phase in during their controller's next untap
 * step even without the phasing keyword (CR 702.26a). Phasing is no zone change: nothing leaves or enters the
 * battlefield.
 *
 * <p>One-shot. Subjects: {@code source()} ("this creature phases out"; Mist Dragon), {@code target()} ("target
 * permanent phases out"; Reality Ripple, the only one that declares a permanent target, narrowed by a
 * {@code PermanentPredicateTargetFilter} on the card), {@code attached()} ("enchanted creature phases out";
 * Vanishing), {@code named(id)} (the attacker a delayed end-of-combat ability phases out; Teferi's Veil) and a
 * {@code controlledBy} set ("all lands you control phase out", Taniwha; "each creature target player controls",
 * which declares the player target).
 */
public record PhaseOutEffect(PermanentSubject subject) implements CardEffect {

    public PhaseOutEffect {
        Objects.requireNonNull(subject, "subject");
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Target) {
            return TargetSpec.harmful(TargetPredicates.permanent());
        }
        if (subject instanceof PermanentSubject.ControlledBy controlledBy && controlledBy.players().declaresTarget()) {
            return TargetSpec.harmful(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }
}
