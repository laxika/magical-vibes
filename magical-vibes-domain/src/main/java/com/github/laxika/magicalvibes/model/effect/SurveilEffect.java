package com.github.laxika.magicalvibes.model.effect;

/**
 * Surveil N — look at the top N cards of your library, then put any number
 * of them into your graveyard and the rest back on top in any order.
 *
 * <p>For surveil 1, this is modelled as a may-ability: "Put [card name]
 * into your graveyard?" If accepted, the top card goes to the graveyard;
 * if declined, it stays on top.
 *
 * @param count the number of cards to surveil (currently only 1 is supported)
 */
public record SurveilEffect(int count) implements CardEffect {
}
