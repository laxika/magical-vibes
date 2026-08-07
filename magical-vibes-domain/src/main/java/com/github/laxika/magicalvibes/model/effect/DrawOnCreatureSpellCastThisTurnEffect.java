package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a turn-scoped delayed trigger: "Whenever you cast a creature spell this turn,
 * draw a card."
 * <p>
 * Tracked via {@code GameData.creatureSpellCastDrawsThisTurn}: the counter is the number of
 * cards drawn per creature spell cast (so two copies of Glimpse of Nature draw two cards each),
 * and it is cleared at end of turn.
 * <p>
 * Used by Glimpse of Nature.
 */
public record DrawOnCreatureSpellCastThisTurnEffect() implements CardEffect {
}
