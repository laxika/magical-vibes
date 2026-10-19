package com.github.laxika.magicalvibes.model.amount;

/**
 * The mana value of the card most recently exiled with the source permanent, or 0 when none is
 * linked. Reads only the card exiled by the current resolution, unlike
 * {@link TotalManaValueOfCardsExiledWithSource}: "Exile a permanent card from your graveyard. You gain
 * life equal to its mana value" (The Aesir Escape Valhalla chapter I, which can repeat).
 */
public record ManaValueOfLastCardExiledWithSource() implements DynamicAmount {
}
