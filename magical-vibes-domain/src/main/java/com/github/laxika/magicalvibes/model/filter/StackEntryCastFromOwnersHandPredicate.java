package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches stack entries for spells cast from their owner's hand: from any hand, as a card in a hand is in its owner's
 * (CR 400.3), so a spell cast from another player's hand (Sen Triplets) was too. Wash Away's "a spell that wasn't cast
 * from its owner's hand" negates it; "from your hand" is {@code StackEntryCastFromZonePredicate(Zone.HAND)}.
 */
public record StackEntryCastFromOwnersHandPredicate() implements StackEntryPredicate {
}
