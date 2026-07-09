package com.github.laxika.magicalvibes.model.effect;

/**
 * "All damage that would be dealt to target creature this turn by a source of your choice is dealt
 * to this creature instead." The target creature is the ability's target; the source is chosen on
 * resolution (not a target); the redirected damage is dealt to the source permanent of this ability.
 * Used by Oracle's Attendants.
 */
public record RedirectTargetCreatureDamageFromChosenSourceToSelfEffect() implements CardEffect {
    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
