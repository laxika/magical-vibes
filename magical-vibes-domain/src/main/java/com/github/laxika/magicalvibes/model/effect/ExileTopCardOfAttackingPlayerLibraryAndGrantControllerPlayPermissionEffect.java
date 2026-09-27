package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the attacking player's library and lets the source's controller play it
 * for as long as it remains exiled, spending mana as though it were mana of any color.
 */
public record ExileTopCardOfAttackingPlayerLibraryAndGrantControllerPlayPermissionEffect()
        implements CardEffect {
}
