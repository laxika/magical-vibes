package com.github.laxika.magicalvibes.model.effect;

/**
 * Saving Grace's enters trigger: "all damage that would be dealt this turn to you and permanents you
 * control is dealt to enchanted creature instead." On resolution its handler installs a turn-long
 * {@code TurnDamageRedirectToCreatureShield} onto the aura's controller pointing at the creature the
 * aura is currently attached to (locking that creature in for the rest of the turn).
 */
public record RedirectYourDamageToEnchantedCreatureThisTurnEffect() implements CardEffect {
}
