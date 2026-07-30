package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents the next X damage that would be dealt this turn to the effect's controller and/or the
 * permanents they control. Each time damage is prevented this way, the source card deals that much
 * damage to the target chosen as the spell was cast (e.g. Divine Deflection).
 *
 * <p>The wider {@code coversControlledPermanents} variant of Vengeful Archon's shield: the same
 * {@code DamageRedirectShield} record backs both, and the resulting damage is dealt to any target
 * rather than only to a player.
 */
public record PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.ANY_TARGET);
    }
}
