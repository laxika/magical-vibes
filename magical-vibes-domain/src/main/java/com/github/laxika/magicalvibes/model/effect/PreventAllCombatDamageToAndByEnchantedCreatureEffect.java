package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all combat damage that would be dealt to and dealt by enchanted creature" (Gaseous Form), or
 * with {@link Subjects#source()} "... by this creature" (Fog Bank).
 * <p>
 * The damage-taken side is checked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#creatureDamageSteps()}, the damage-dealt side
 * in {@code GameQueryService.isPreventedFromDealingDamage(GameData, Permanent, boolean)}. Noncombat damage in either
 * direction is unaffected.
 *
 * @param subject whose damage is prevented: {@link Subjects#source()} or the attachment's host
 *                ({@link Subjects#attachedCreature()})
 */
public record PreventAllCombatDamageToAndByEnchantedCreatureEffect(PermanentSubject subject) implements CardEffect {

    public PreventAllCombatDamageToAndByEnchantedCreatureEffect {
        if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Attached)) {
            throw new IllegalArgumentException("Protects the source or the attached permanent: " + subject);
        }
    }

    public PreventAllCombatDamageToAndByEnchantedCreatureEffect() {
        this(Subjects.attachedCreature());
    }
}
