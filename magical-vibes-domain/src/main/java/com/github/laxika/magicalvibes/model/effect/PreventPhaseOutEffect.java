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
 * <p>{@link PhaseOutSubject#TARGET} is the chosen-permanent form (Spatial Binding) and is the only
 * subject that declares a {@code TargetSpec}; {@link PhaseOutSubject#SOURCE} protects the ability's
 * own permanent ("this creature can't phase out"; Ertai's Familiar).
 * {@link PhaseOutSubject#ATTACHED} is not supported.
 */
public record PreventPhaseOutEffect(PhaseOutSubject subject) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return subject == PhaseOutSubject.TARGET
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
