package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the controller permission to cast this effect's source card from exile for its normal cost.
 *
 * @param thisTurnOnly {@code true} for "this turn" (Heaven Sent); {@code false} for "for as long as it
 *                     remains exiled" (Glamorous Outlaw and the other SNC hand-exile creatures), which
 *                     only applies if the ability resolves
 */
public record AllowCastSourceCardFromExileEffect(boolean thisTurnOnly) implements CardEffect {

    /** "You may cast it this turn." */
    public static AllowCastSourceCardFromExileEffect thisTurn() {
        return new AllowCastSourceCardFromExileEffect(true);
    }

    /** "You may cast this card for as long as it remains exiled." */
    public static AllowCastSourceCardFromExileEffect whileExiled() {
        return new AllowCastSourceCardFromExileEffect(false);
    }
}
