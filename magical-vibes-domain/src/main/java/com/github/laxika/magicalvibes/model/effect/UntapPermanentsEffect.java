package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Untaps permanent(s) as an effect. The {@link PermanentSubject} names which permanent(s) are untapped:
 * {@code target()} ("untap target creature"; {@code filter} restricts what can be targeted, carried on the
 * {@link #targetSpec()} predicate), {@code allTargets()} ("untap up to four target permanents" whose targets the
 * ability's target filter or slots declare), {@code source()} ("untap this creature"), {@code attached()},
 * {@code triggering()}, or a set such as {@code controlledBy(you(), creature)} ("untap all creatures you control"),
 * whose filter is part of the subject.
 *
 * <p>Replaces the former {@code UntapTargetPermanentEffect}, {@code UntapAllTargetPermanentsEffect},
 * {@code UntapSelfEffect}, {@code UntapAllControlledPermanentsEffect},
 * {@code UntapEachOtherCreatureYouControlEffect}, {@code UntapAttackedCreaturesEffect},
 * {@code UntapEquippedCreatureEffect} and {@code UntapUpToControlledPermanentsEffect}.
 *
 * <p>With a set subject, {@code chosenCount} of 0 untaps every tapped permanent in it; a positive
 * {@code chosenCount} instead lets the controller pick <em>up to</em> that many of its tapped permanents to untap
 * (Rewind, Unwind, Treachery), mirroring {@link TapPermanentsEffect#chosenCount()}.
 *
 * @param subject     which permanent(s) to untap
 * @param filter      the targeting restriction of a {@code target()} subject (null = no restriction); a set subject
 *                    carries its own filter
 * @param chosenCount 0 = untap every permanent the subject names; &gt;0 = the controller chooses up to N of a set
 */
public record UntapPermanentsEffect(PermanentSubject subject, PermanentPredicate filter, int chosenCount)
        implements CardEffect {

    public UntapPermanentsEffect {
        Objects.requireNonNull(subject, "subject");
        if (filter != null && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("Only a target subject takes a target filter: " + subject);
        }
        if (chosenCount < 0 || chosenCount > 0 && !subject.isSet()) {
            throw new IllegalArgumentException("Choosing up to N permanents needs a set subject: " + subject);
        }
    }

    public UntapPermanentsEffect(PermanentSubject subject) {
        this(subject, null, 0);
    }

    public UntapPermanentsEffect(PermanentSubject subject, PermanentPredicate filter) {
        this(subject, filter, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            // A single chosen permanent (filter narrows what may be targeted).
            case PermanentSubject.Target ignored -> TargetSpec.benign(TargetPredicates.permanent(), filter);
            // Garruk Wildspeaker's "untap two target lands": its targets ride entry.getTargetIds() and are validated
            // on the multi-target path, so the spec stays a no-op (PLAYER_OR_PERMANENT) to keep that path's null
            // tolerance.
            case PermanentSubject.AllTargets ignored -> TargetSpec.benign(TargetPredicates.playerOrPermanent());
            case PermanentSubject.ControlledBy controlledBy when controlledBy.players().declaresTarget() ->
                    TargetSpec.benign(TargetPredicates.player());
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
