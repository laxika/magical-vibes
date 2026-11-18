package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A regeneration shield (CR 701.19a): the next time the permanent the floating effect carrying it names would be
 * destroyed this turn, it regenerates instead, and the shield ends. The effect's controller is the player who
 * controlled the spell or ability that made it.
 *
 * @param rider    what else happens when the permanent regenerates this way
 * @param playerId the opponent who may draw a card for {@link Rider#OPPONENT_DRAW}, otherwise {@code null}
 */
public record RegenerationShield(Rider rider, UUID playerId) implements CardEffect {

    /** A shield with no rider. */
    public static final RegenerationShield PLAIN = new RegenerationShield(Rider.NONE, null);

    /** What a regeneration shield does besides regenerating, once it is used. */
    public enum Rider {
        /** Nothing else. */
        NONE,
        /** "When it regenerates this way, put a -1/-1 counter on it" (Matopi Golem). */
        MINUS_ONE_COUNTER,
        /**
         * "When it regenerates this way, put a +1/+1 counter on it" (Skeleton Scavengers): a delayed triggered
         * ability controlled by the effect's controller (CR 603.7e).
         */
        PLUS_ONE_COUNTER,
        /**
         * "When it regenerates this way, that player may draw a card" (Soldevi Sentry): a delayed triggered ability
         * controlled by the effect's controller (CR 603.7e), offering the card to {@link #playerId()}.
         */
        OPPONENT_DRAW,
        /** "You gain control of that creature if it regenerates this way" (Debt of Loyalty): the effect's controller. */
        GAIN_CONTROL
    }
}
