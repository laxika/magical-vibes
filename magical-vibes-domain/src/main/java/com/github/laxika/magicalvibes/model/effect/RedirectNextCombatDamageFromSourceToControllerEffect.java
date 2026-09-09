package com.github.laxika.magicalvibes.model.effect;

/**
 * Installs a one-shot replacement shield for the source permanent: its next combat damage this turn
 * is dealt to the controller of the triggered ability instead.
 */
public record RedirectNextCombatDamageFromSourceToControllerEffect() implements CardEffect {
}
