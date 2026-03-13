package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents the next X damage that would be dealt to the ability's controller this turn.
 * If damage is prevented this way, the source creature deals that much damage to
 * target player or planeswalker (e.g. Vengeful Archon).
 */
public record PreventXDamageToControllerAndRedirectToTargetPlayerEffect() implements CardEffect {
    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
