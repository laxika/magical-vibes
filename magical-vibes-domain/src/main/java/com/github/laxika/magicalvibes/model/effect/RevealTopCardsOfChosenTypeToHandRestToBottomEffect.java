package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top cards of the controller's library, putting cards of a chosen type into their
 * hand and the rest on the bottom of their library in any order.
 */
public record RevealTopCardsOfChosenTypeToHandRestToBottomEffect(int count) implements CardEffect {}
