package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/**
 * One-shot type-changing effect (CR 613.1d layer 4) that gives the targeted permanent a supertype
 * or takes one away for as long as it remains on the battlefield. Arcum's Weathervane uses both
 * directions: "target snow land is no longer snow" is {@code (SNOW, false)} and "target nonsnow
 * basic land becomes snow" is {@code (SNOW, true)}.
 *
 * <p>The record is also its own payload: the resolution puts it on the target as a floating effect with no end
 * (CR 611.2a), applied in layer 4 at its timestamp and read back through
 * {@code GameQueryService.hasEffectiveSupertype}, where the newest change on the permanent decides.
 *
 * <p>The spec is left as an unnarrowed benign permanent target — which permanents are legal
 * ("snow land", "nonsnow basic land") differs per ability and is expressed with the
 * {@code ActivatedAbility}'s {@code TargetFilter}.
 *
 * <p>A non-target {@link #subject} names the permanents some other way ({@code source()}: Tenth District Hero's "it
 * becomes a legendary creature"), found once as the effect resolves (CR 611.2c).
 *
 * @param supertype the supertype to grant or remove
 * @param gained    {@code true} to grant the supertype, {@code false} to remove it
 * @param subject   the permanents that change
 */
public record SetTargetPermanentSupertypeEffect(CardSupertype supertype, boolean gained, PermanentSubject subject)
        implements CardEffect {

    public SetTargetPermanentSupertypeEffect {
        java.util.Objects.requireNonNull(subject, "subject");
    }

    /** "Target [permanent] becomes / is no longer [supertype]". */
    public SetTargetPermanentSupertypeEffect(CardSupertype supertype, boolean gained) {
        this(supertype, gained, Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
