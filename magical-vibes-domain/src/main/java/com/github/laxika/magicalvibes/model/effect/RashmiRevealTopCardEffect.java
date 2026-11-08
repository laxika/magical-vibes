package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. A nonland card with mana value less than that of the spell that
 * caused the ability to trigger (the stack entry's triggering spell, as it last existed on the stack once it has
 * left, {@code TargetSpellManaValue}) may be cast without paying its mana cost; otherwise, or if declined, the
 * revealed card is put into its controller's hand. Rashmi, Eternities Crafter: {@code Triggers.youCastSpell()
 * .nthThisTurn(1)}.
 */
public record RashmiRevealTopCardEffect() implements CardEffect {
}
