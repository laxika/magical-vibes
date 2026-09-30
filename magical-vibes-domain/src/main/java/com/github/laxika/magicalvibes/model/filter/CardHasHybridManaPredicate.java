package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards whose mana cost contains at least one ordinary hybrid mana symbol.
 */
public record CardHasHybridManaPredicate() implements CardPredicate {
}
