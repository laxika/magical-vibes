package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If combat damage would be dealt to this creature, prevent that damage and exile
 * that many cards from the top of your library." (Gloom Surgeon)
 * <p>
 * Only combat damage qualifies — noncombat damage (burn, bite, mass damage) is unaffected. The cards
 * are exiled from the library of the permanent's controller. Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyPreventCombatDamageToSelfAndExile}.
 */
public record PreventCombatDamageToSelfAndExileFromLibraryEffect() implements CardEffect {
}
