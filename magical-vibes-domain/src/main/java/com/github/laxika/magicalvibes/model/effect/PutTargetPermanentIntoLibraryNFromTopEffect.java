package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Puts the permanent {@code subject} names into its owner's library at a specific position from the top.
 * Position is 0-indexed: 0 = top, 1 = second from top, 2 = third from top, etc.
 *
 * <p>{@link Subjects#target()} (the default) targets a permanent; {@link Subjects#source()} ({@link #self}) is "put
 * this permanent into its owner's library"; {@link Subjects#attached()} is "put enchanted creature into its owner's
 * library" (Shattered Ego), the creature the Aura enchants as the ability resolves, else as the Aura last existed
 * (CR 608.2h).
 *
 * @param position the dynamic 0-indexed position from the top of the library
 * @param subject  the permanent put into its owner's library
 */
public record PutTargetPermanentIntoLibraryNFromTopEffect(DynamicAmount position, PermanentSubject subject)
        implements CardEffect {

    public PutTargetPermanentIntoLibraryNFromTopEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
        if (!(subject instanceof PermanentSubject.Target) && !(subject instanceof PermanentSubject.Source)
                && !(subject instanceof PermanentSubject.Attached)) {
            throw new IllegalArgumentException("Puts the target, the source or the attached permanent: " + subject);
        }
    }

    public PutTargetPermanentIntoLibraryNFromTopEffect(DynamicAmount position) {
        this(position, Subjects.target());
    }

    public PutTargetPermanentIntoLibraryNFromTopEffect(int position) {
        this(new Fixed(position), Subjects.target());
    }

    /** Puts {@code subject} into its owner's library {@code position} from the top. */
    public PutTargetPermanentIntoLibraryNFromTopEffect(PermanentSubject subject, int position) {
        this(new Fixed(position), subject);
    }

    /** Puts {@code subject} into its owner's library {@code position} from the top. */
    public PutTargetPermanentIntoLibraryNFromTopEffect(PermanentSubject subject, DynamicAmount position) {
        this(position, subject);
    }

    /** "Put this permanent into its owner's library {@code position} from the top." */
    public static PutTargetPermanentIntoLibraryNFromTopEffect self(int position) {
        return new PutTargetPermanentIntoLibraryNFromTopEffect(new Fixed(position), Subjects.source());
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        // The Aura form keeps the self-targeting marker the old enchanted record declared.
        return subject instanceof PermanentSubject.Attached
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.NONE;
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }
}
