package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage to you this turn, that damage is dealt
 * to target creature you control instead", or to this creature instead (Beacon of Destiny:
 * {@link Subjects#source()}, not targeted). The source of the damage is chosen on resolution, while a
 * creature is targeted when the ability is activated.
 *
 * @param subject the creature the damage is dealt to: the target or the ability's source
 */
public record RedirectNextDamageFromChosenSourceToTargetCreatureEffect(PermanentSubject subject)
        implements CardEffect {

    public RedirectNextDamageFromChosenSourceToTargetCreatureEffect {
        if (!subject.equals(Subjects.target()) && !subject.equals(Subjects.source())) {
            throw new IllegalArgumentException("Unsupported redirect subject: " + subject);
        }
    }

    public RedirectNextDamageFromChosenSourceToTargetCreatureEffect() {
        this(Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.harmful(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
