package com.github.laxika.magicalvibes.model.amount;

/**
 * The toughness of the card most recently exiled with the source permanent, evaluated when the
 * amount is read (so a characteristic-defined toughness reflects the game state then). Falls back to
 * the stack entry's event value when no exiled card is linked to the source any more: "Exile target
 * creature card from a graveyard. When you do, put X +1/+1 counters on target Symbiote, where X is
 * the exiled card's toughness" (Venom, Deadly Devourer).
 */
public record ToughnessOfLastCardExiledWithSource() implements DynamicAmount {
}
