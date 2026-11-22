package com.github.laxika.magicalvibes.model.effect;


import java.util.Objects;

/**
 * "All damage that would be dealt this turn to target creature by a source of your choice is dealt to another target
 * creature instead" (Kor Chant: target groups 0 and 1), or "... is dealt to this creature instead"
 * ({@link #toSource()}, Oracle's Attendants). The source is chosen on resolution and isn't a target.
 *
 * @param protectedCreature the targeted creature whose damage is redirected
 * @param redirectTo        who is dealt it instead: a second target, or the ability's source
 */
public record RedirectTargetCreatureDamageFromChosenSourceToTargetEffect(
        PermanentSubject protectedCreature,
        PermanentSubject redirectTo
) implements CardEffect {

    public RedirectTargetCreatureDamageFromChosenSourceToTargetEffect {
        Objects.requireNonNull(protectedCreature, "protectedCreature");
        if (!(redirectTo instanceof PermanentSubject.Target || redirectTo instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Redirects to a target or to the source: " + redirectTo);
        }
    }

    public RedirectTargetCreatureDamageFromChosenSourceToTargetEffect() {
        this(Subjects.target(0), Subjects.target(1));
    }

    /** "... is dealt to this creature instead": one target, redirected to the ability's source. */
    public static RedirectTargetCreatureDamageFromChosenSourceToTargetEffect toSource() {
        return new RedirectTargetCreatureDamageFromChosenSourceToTargetEffect(Subjects.target(), Subjects.source());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
