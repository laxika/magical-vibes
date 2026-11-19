package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles the permanents {@code subject} names: by default the targeted permanent(s) (also multi-target via
 * targetIds), else "this permanent" ({@link Subjects#source()}), "enchanted creature" / "equipped creature"
 * ({@link Subjects#attached()}), "that creature" of a trigger ({@link Subjects#triggering()}). Optionally creates a
 * token for each exiled target's controller (e.g. Crib Swap creates a 1/1 Shapeshifter for the target's controller).
 *
 * @param tokenForController if non-null, creates this token for each exiled target's controller; target subject only
 * @param targetFilter       optional predicate narrowing the permanent target; target subject only
 * @param subject            which permanents are exiled; {@code null} means {@link Subjects#target()}. Only a target
 *                           subject declares a target, so "exile this creature" or "exile enchanted creature" is not
 *                           stopped by shroud or protection
 * @param outsideGame        the permanent is removed from the game instead of exiled ("tear this into pieces ...
 *                           remove the pieces from the game", Blacker Lotus); non-target subject only
 */
public record ExileTargetPermanentEffect(CreateTokenEffect tokenForController,
                                         PermanentPredicate targetFilter,
                                         PermanentSubject subject,
                                         boolean outsideGame)
        implements RemovalEffect {

    public ExileTargetPermanentEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
        if (!(subject instanceof PermanentSubject.Target)) {
            if (tokenForController != null || targetFilter != null) {
                throw new IllegalArgumentException("A token for the target's controller and a target filter need a "
                        + "target subject: " + subject);
            }
        } else if (outsideGame) {
            throw new IllegalArgumentException("Removing from the game needs a subject that is not a target");
        }
    }

    public ExileTargetPermanentEffect() {
        this(null, null);
    }

    public ExileTargetPermanentEffect(CreateTokenEffect tokenForController, PermanentPredicate targetFilter) {
        this(tokenForController, targetFilter, null, false);
    }

    /** Exiles what {@code subject} names. */
    public ExileTargetPermanentEffect(PermanentSubject subject) {
        this(null, null, subject, false);
    }

    /**
     * "Exile that creature": the stack entry's triggering permanent (Pit Spawn, Sword of Kaldra), exiled only if it is
     * still on the battlefield.
     */
    public static ExileTargetPermanentEffect ofTriggeringPermanent() {
        return new ExileTargetPermanentEffect(Subjects.triggering());
    }

    /** Removes the source permanent from the game instead of exiling it (Blacker Lotus). */
    public static ExileTargetPermanentEffect sourceOutsideGame() {
        return new ExileTargetPermanentEffect(null, null, Subjects.source(), true);
    }

    public ExileTargetPermanentEffect(CreateTokenEffect tokenForController) {
        this(tokenForController, null);
    }

    public ExileTargetPermanentEffect(PermanentPredicate targetFilter) {
        this(null, targetFilter);
    }

    @Override
    public TargetSpec targetSpec() {
        if (!subject.declaresTarget()) {
            return TargetSpec.NONE;
        }
        return targetFilter == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetFilter);
    }

    /**
     * Single-permanent exile removal for a target and for "exile that creature" of a trigger; "exile this" and
     * "exile enchanted creature" are not.
     */
    @Override
    public RemovalKind removalKind() {
        return subject.declaresTarget() || subject instanceof PermanentSubject.Triggering ? RemovalKind.EXILE : null;
    }

    /**
     * Binds the enchanted permanent as the entry's target when an Aura's activated ability exiles "enchanted
     * creature", so a sacrifice-the-Aura cost does not lose it.
     */
    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }
}
