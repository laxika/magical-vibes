package com.github.laxika.magicalvibes.model;

/**
 * A named counter that {@code PutCounterOnExiledCardsEffect} puts on a card in exile (CR 122.1). Each has its own
 * store on {@link GameData}, which the abilities reading "cards in exile with [name] counters on them" query and
 * which forgets a card as it leaves exile (CR 400.7).
 */
public enum ExiledCardCounter {
    /** Altaïr Ibn-La'Ahad, The Animus. */
    MEMORY,
    /** Rex, Cyber-Hound. */
    BRAIN,
    /** Grolnok, the Omnivore. */
    CROAK,
    /** Pako, Arcane Retriever: the store also keeps the player whose effect put it there as the card's exiler. */
    FETCH
}
