package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect for Harsh Judgment: if an instant or sorcery spell of the permanent's
 * chosen color would deal damage to that permanent's controller, it deals that damage to the
 * spell's controller instead.
 */
public record RedirectChosenColorSpellDamageToControllerEffect() implements CardEffect {
}
