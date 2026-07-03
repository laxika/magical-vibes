package com.github.laxika.magicalvibes.model;

/**
 * Karn, Scion of Urza &minus;1: marks that the active {@code LIBRARY_REVEAL_CHOICE} is the
 * controller choosing a silver-counter card to return from exile to their hand.
 */
public record PendingKarnScionExileReturn() implements PendingInteraction {
}
