package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the enchanted permanent "At the beginning of your end step, sacrifice this permanent and
 * attach [this Aura] to a creature or land you control." Placed in the
 * {@code ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED} slot; resolved by
 * {@code SacrificeEnchantedPermanentAndReattachSourceAuraEffectHandler}. Used by Nettlevine Blight.
 *
 * <p>The enchanted permanent's controller sacrifices it, then moves the source Aura onto one of
 * their creatures or lands. The Aura never changes controller. If that player has no other creature
 * or land, the enchanted permanent is still sacrificed and the now-unattached Aura is put into its
 * owner's graveyard as a state-based action.
 */
public record SacrificeEnchantedPermanentAndReattachSourceAuraEffect() implements CardEffect {
}
