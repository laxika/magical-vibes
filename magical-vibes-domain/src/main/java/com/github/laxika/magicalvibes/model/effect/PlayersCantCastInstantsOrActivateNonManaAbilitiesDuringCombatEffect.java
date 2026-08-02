package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: during combat, no player can cast instant spells or activate abilities that
 * aren't mana abilities. Symmetric — it applies to every player, in every zone. Used by
 * Hand to Hand (TMP).
 */
public record PlayersCantCastInstantsOrActivateNonManaAbilitiesDuringCombatEffect() implements CardEffect {
}
