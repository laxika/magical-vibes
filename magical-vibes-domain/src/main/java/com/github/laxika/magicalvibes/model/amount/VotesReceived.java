package com.github.laxika.magicalvibes.model.amount;

import java.util.Objects;

/**
 * "For each vote they received", "for each vote you received", "that many" after "for each creature with one or more
 * votes": how many votes the most recent vote of the resolving entry ({@code VoteEffect}, its VOTE step) cast for one
 * player or permanent, additional votes included (CR 701.38d). 0 when the entry had no vote, and outside a resolution.
 *
 * @param of whose votes are counted
 */
public record VotesReceived(Of of) implements DynamicAmount {

    /** Whose votes a {@link VotesReceived} counts. */
    public enum Of {
        /**
         * The player the amount is evaluated for: "you", or "they" of a verb that evaluates its amount for each player
         * ("each player draws a card for each vote they received").
         */
        PLAYER,
        /** The permanent the amount is evaluated against ("put that many stun counters on it"). */
        PERMANENT
    }

    public VotesReceived {
        Objects.requireNonNull(of, "of");
    }

    /** The votes the player the amount is evaluated for received. */
    public static VotesReceived byPlayer() {
        return new VotesReceived(Of.PLAYER);
    }

    /** The votes the permanent the amount is evaluated against received. */
    public static VotesReceived byPermanent() {
        return new VotesReceived(Of.PERMANENT);
    }
}
