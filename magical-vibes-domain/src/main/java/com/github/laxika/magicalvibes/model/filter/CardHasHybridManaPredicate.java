package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards whose mana cost contains at least one hybrid mana symbol, including hybrid Phyrexian symbols.
 */
public record CardHasHybridManaPredicate() implements CardPredicate {
}
