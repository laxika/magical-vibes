package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile all other spells on the stack and counter all activated and triggered abilities on the
 * stack (Summary Dismissal). Spells are exiled even if they can't be countered; only abilities are
 * subject to uncounterable / counter-protection checks. Non-targeting.
 */
public record ExileOtherSpellsAndCounterAbilitiesEffect() implements CardEffect {
}
