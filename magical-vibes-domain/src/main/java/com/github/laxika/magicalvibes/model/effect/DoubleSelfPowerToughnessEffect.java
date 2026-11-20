package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the power and toughness of the creature {@code subject} names until end of turn, or only its power when
 * {@code powerOnly} is set (Casey Jones, Asphalt Hooligan): the source creature (the default) or target creature
 * ({@link #ofTargetCreature()}: Epic Fight, Dragonclaw Strike, Nylea's Colossus).
 * <p>
 * Per CR 701.10b the creature gets +X/+Y where X and Y are its power and toughness as the ability
 * resolves; a negative value is doubled downward rather than clamped (CR 701.10c).
 */
public record DoubleSelfPowerToughnessEffect(boolean powerOnly, PermanentSubject subject)
        implements CardEffect, DoublingEffect {

    public DoubleSelfPowerToughnessEffect {
        if (subject == null) {
            subject = Subjects.source();
        }
        if (!(subject instanceof PermanentSubject.Source) && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("Doubles the source or the target creature: " + subject);
        }
    }

    public DoubleSelfPowerToughnessEffect(boolean powerOnly) {
        this(powerOnly, Subjects.source());
    }

    public DoubleSelfPowerToughnessEffect() {
        this(false);
    }

    /** Doubles only the source creature's power. */
    public static DoubleSelfPowerToughnessEffect power() {
        return new DoubleSelfPowerToughnessEffect(true);
    }

    /** "Double target creature's power and toughness until end of turn." */
    public static DoubleSelfPowerToughnessEffect ofTargetCreature() {
        return new DoubleSelfPowerToughnessEffect(false, Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.creature())
                : new TargetSpec(null, false, null, true, 1);
    }
}
