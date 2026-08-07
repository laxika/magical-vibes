package com.github.laxika.magicalvibes.model.effect;

/**
 * Non-targeting-by-itself ability effect: the player carried as the stack entry's target can't
 * activate abilities that aren't mana abilities for the rest of this turn. Cleared at end of turn.
 * Loyalty abilities are blocked too — only mana abilities stay usable. Used by Abeyance.
 */
public record TargetPlayerCantActivateNonManaAbilitiesThisTurnEffect() implements CardEffect {
}
