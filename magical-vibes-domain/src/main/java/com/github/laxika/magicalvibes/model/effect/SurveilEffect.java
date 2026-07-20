package com.github.laxika.magicalvibes.model.effect;

/**
 * Surveil N — look at the top N cards of your library, then put any number
 * of them into your graveyard and the rest back on top in any order.
 *
 * <p>Surveil 1 is modelled as a may-ability: "Put [card name] into your
 * graveyard?" If accepted, the top card goes to the graveyard; if declined,
 * it stays on top. Surveil 2+ uses the shared SCRY_ORDER split interaction
 * (see {@code PendingInteraction.Scry} with {@code toGraveyard = true}): the
 * player looks at the top N cards and splits them between the top of the
 * library (in any order) and the graveyard.
 *
 * @param count the number of cards to surveil
 */
public record SurveilEffect(int count) implements CardEffect {
}
