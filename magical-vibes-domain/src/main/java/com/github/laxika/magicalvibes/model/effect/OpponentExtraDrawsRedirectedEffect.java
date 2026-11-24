package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Static draw-replacement effect: if an opponent of the source's controller would draw a card except the first one
 * they draw in each of their draw steps, instead that player skips that draw and the controller gets the
 * {@link Payload}: a card drawn (Notion Thief) or a Treasure token created (Hullbreacher).
 *
 * @param payload what the controller gets instead of the opponent's draw
 */
public record OpponentExtraDrawsRedirectedEffect(Payload payload) implements CardEffect {

    /** What the source's controller gets in place of the replaced draw. */
    public enum Payload {
        /** "you draw a card". */
        DRAW_A_CARD,
        /** "you create a Treasure token". */
        CREATE_A_TREASURE
    }

    public OpponentExtraDrawsRedirectedEffect {
        Objects.requireNonNull(payload, "payload");
    }

    /** "That player skips that draw and you draw a card." */
    public OpponentExtraDrawsRedirectedEffect() {
        this(Payload.DRAW_A_CARD);
    }

    /** "Instead you create a Treasure token." */
    public static OpponentExtraDrawsRedirectedEffect createTreasureInstead() {
        return new OpponentExtraDrawsRedirectedEffect(Payload.CREATE_A_TREASURE);
    }
}
