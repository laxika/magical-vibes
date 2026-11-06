package com.github.laxika.magicalvibes.model.effect;

/**
 * "Put the exiled card into its owner's hand": the card the source's linked ability exiled (CR 607.2a). Used by
 * Hoarding Dragon's death trigger, which reads the stack entry's linked exiled card
 * ({@code StackEntry.getLinkedExiledCardId()}), fixed as the ability triggered so the Dragon returning and exiling a
 * different card can't redirect it; without one it reads the source's imprint as it resolves.
 */
public record PutImprintedCardIntoOwnersHandEffect() implements CardEffect {
}
