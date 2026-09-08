package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: prevent all combat damage that would be dealt to and dealt by creatures the
 * source's controller controls. The affected creatures are evaluated when damage would be dealt,
 * so the effect follows control changes and applies to creatures entering later.
 */
public record PreventAllCombatDamageToAndByCreaturesYouControlEffect() implements CardEffect {
}
