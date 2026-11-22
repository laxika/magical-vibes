package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all combat damage that would be dealt to this creature." (e.g. Seraph of the Sword), or with
 * {@link Subjects#attachedCreature()} on an Aura or Equipment "... to enchanted / equipped creature" (General's Kabuto).
 * <p>
 * Read in {@link com.github.laxika.magicalvibes.service.DamagePreventionService#creatureDamageSteps()}. Noncombat
 * damage is unaffected, and unlike {@link PreventAllCombatDamageToAndByEnchantedCreatureEffect} the creature still
 * deals its own combat damage.
 *
 * @param subject whose damage is prevented: {@link Subjects#source()} or the attachment's host
 *                ({@link Subjects#attachedCreature()})
 */
public record PreventAllCombatDamageToSelfEffect(PermanentSubject subject) implements CardEffect {

    public PreventAllCombatDamageToSelfEffect {
        if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Attached)) {
            throw new IllegalArgumentException("Protects the source or the attached permanent: " + subject);
        }
    }

    public PreventAllCombatDamageToSelfEffect() {
        this(Subjects.source());
    }
}
