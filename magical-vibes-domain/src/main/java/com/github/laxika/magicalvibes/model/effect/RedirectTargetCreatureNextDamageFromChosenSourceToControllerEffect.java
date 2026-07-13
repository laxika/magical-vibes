package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage to target creature this turn, that source deals
 * that damage to you instead." The target creature is the ability's target; the source is chosen on
 * resolution (not a target); the redirected damage is dealt to the ability's controller. Only the next
 * single damage event from the chosen source is redirected, then the shield is consumed. Used by Jade
 * Monolith.
 */
public record RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect() implements CardEffect {
    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
