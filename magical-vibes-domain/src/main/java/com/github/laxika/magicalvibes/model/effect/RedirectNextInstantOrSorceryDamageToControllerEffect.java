package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time an instant or sorcery spell would deal damage to you this turn, that spell deals
 * that damage to its controller instead." (Aegis of Honor). The one-shot shield is keyed to the
 * activating player's incoming damage and only matches a spell dealing damage as itself.
 */
public record RedirectNextInstantOrSorceryDamageToControllerEffect() implements CardEffect {
}
