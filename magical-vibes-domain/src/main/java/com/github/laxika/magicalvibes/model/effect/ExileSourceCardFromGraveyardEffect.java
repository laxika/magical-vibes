package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved as a death trigger, exiles the dying creature's card from its owner's
 * graveyard. Uses the stack entry's card (the source of the trigger) to identify which
 * card to exile.
 *
 * <p>This is the exile analogue of
 * {@link PutSourceCardFromGraveyardIntoLibraryNFromTopEffect} and
 * {@link ReturnSourceCardFromGraveyardToOwnerHandEffect}. Used by Cyclopean Mummy
 * ("When this creature dies, exile it.").
 */
public record ExileSourceCardFromGraveyardEffect() implements CardEffect {
}
