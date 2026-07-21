package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect placed on a Curse Aura that enchants a player. The enchanted player can't activate
 * activated abilities that aren't mana abilities or loyalty abilities (Overwhelming Splendor).
 *
 * <p>Enforced imperatively in {@code AbilityActivationService} via
 * {@code GameQueryService.playerCantActivateNonManaOrLoyaltyAbilities}: mana abilities (CR 605.1a)
 * and loyalty abilities remain usable; every other activated ability the enchanted player would
 * activate (from the battlefield, graveyard, or hand) is blocked.
 */
public record EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect() implements CardEffect {
}
