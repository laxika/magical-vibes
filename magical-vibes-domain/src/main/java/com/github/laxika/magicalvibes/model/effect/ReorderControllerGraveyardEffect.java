package com.github.laxika.magicalvibes.model.effect;

/**
 * "Reorder your graveyard as you choose": the controller puts their whole graveyard into any
 * order. A graveyard with fewer than two cards has nothing to order and resolves without a
 * decision. Used by Fossil Find (SHM 206) after the random card is returned.
 */
public record ReorderControllerGraveyardEffect() implements CardEffect {
}
