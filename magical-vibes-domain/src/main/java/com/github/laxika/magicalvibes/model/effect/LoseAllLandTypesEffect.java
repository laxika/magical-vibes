package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * A layer-4 type removal (CR 613.1d) from the permanents {@code subject} names that match {@code filter}: by default
 * every land type of a land, without changing its other card types; with {@link #subtypes(Set, PermanentSubject,
 * Duration)} the named subtypes ("that's no longer an Equipment", "isn't a Human"); with
 * {@link #supertype(CardSupertype, PermanentSubject, PermanentPredicate)} a supertype ("all lands are no longer snow",
 * "isn't legendary as long as it's a token"). Removing a subtype or supertype doesn't change card types (CR 205.1a).
 *
 * <p>A static ability applies it to the set as the layered pass finds it; a resolving ability floats it on each
 * permanent the subject names as it resolves, for {@code duration} (CR 611.2c).
 *
 * @param subject   the permanents it applies to
 * @param filter    an optional narrowing of the subject
 * @param subtypes  the subtypes removed, or {@code null} for every land type
 * @param supertype the supertype removed instead of subtypes, or {@code null}
 * @param duration  how long a resolved removal lasts; a static one ignores it
 */
public record LoseAllLandTypesEffect(PermanentSubject subject, PermanentPredicate filter, Set<CardSubtype> subtypes,
                                     CardSupertype supertype, Duration duration) implements CardEffect {

    public LoseAllLandTypesEffect {
        subtypes = subtypes == null ? null : Set.copyOf(subtypes);
    }

    public LoseAllLandTypesEffect(PermanentSubject subject, PermanentPredicate filter) {
        this(subject, filter, null, null, Duration.PERMANENT);
    }

    /** "[Permanents] [are] no longer [subtypes]" for {@code duration}. */
    public static LoseAllLandTypesEffect subtypes(Set<CardSubtype> subtypes, PermanentSubject subject,
                                                  Duration duration) {
        return new LoseAllLandTypesEffect(subject, null, subtypes, null, duration);
    }

    /** "[Permanents matching filter] [are] no longer [supertype]". */
    public static LoseAllLandTypesEffect supertype(CardSupertype supertype, PermanentSubject subject,
                                                   PermanentPredicate filter) {
        return new LoseAllLandTypesEffect(subject, filter, null, supertype, Duration.PERMANENT);
    }

    /** Whether it removes every land type (the default form). */
    public boolean removesLandTypes() {
        return subtypes == null && supertype == null;
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
