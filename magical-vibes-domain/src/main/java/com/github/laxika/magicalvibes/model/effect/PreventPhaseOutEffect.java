package com.github.laxika.magicalvibes.model.effect;

/**
 * "Until your next upkeep, [permanent] can't phase out." A floating effect on the subject permanent until the
 * resolving controller's next upkeep; while it applies the permanent is skipped by every phasing pass — both the untap step's turn-based action (CR 502.1) and one-shot
 * effects that make a permanent phase out. Auras and Equipment attached to it still phase out
 * indirectly (CR 702.26g) only when their host actually phases out, which a marked host never does.
 *
 * <p>The duration clock ends it at the beginning of the resolving controller's next upkeep, one step after the
 * untap step, so a permanent protected on turn N is still protected during the protector's own
 * untap step on turn N+1.
 *
 * <p>{@code target()} is the chosen-permanent form (Spatial Binding) and is the only subject that declares a
 * {@code TargetSpec}; {@code source()} protects the ability's own permanent ("this creature can't phase out";
 * Ertai's Familiar). No other subject is accepted.
 */
public record PreventPhaseOutEffect(PermanentSubject subject) implements CardEffect {

    public PreventPhaseOutEffect {
        if (!(subject instanceof PermanentSubject.Target || subject instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Only this permanent or a target can be kept from phasing out: "
                    + subject);
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
