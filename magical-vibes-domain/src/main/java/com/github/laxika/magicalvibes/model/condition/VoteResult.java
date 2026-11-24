package com.github.laxika.magicalvibes.model.condition;

import java.util.Objects;

/**
 * How the most recent vote of the resolving entry went for one of its options ({@code VoteEffect}, read from the
 * entry's VOTE step, {@code StepOutcome.votes()}): "if [option] gets more votes" ({@link #moreVotes}), "if [option]
 * gets more votes or the vote is tied" ({@link #moreVotesOrTied}), "for each [option] vote" with any such vote
 * ({@link #anyVotes}). "The vote is tied" only when more than one option got the most votes (Plea for Power's ruling):
 * a 3-1-1 vote is not tied. An entry with no vote fails every reading.
 *
 * @param option  the option read
 * @param reading what about it
 */
public record VoteResult(String option, Reading reading) implements Condition {

    /** What a {@link VoteResult} reads about its option. */
    public enum Reading {
        /** It got more votes than each other option. */
        MORE_VOTES,
        /** It got more votes than each other option, or more than one option got the most votes. */
        MORE_VOTES_OR_TIED,
        /** It got at least one vote. */
        ANY_VOTES
    }

    public VoteResult {
        Objects.requireNonNull(option, "option");
        Objects.requireNonNull(reading, "reading");
    }

    /** "If [option] gets more votes". */
    public static VoteResult moreVotes(String option) {
        return new VoteResult(option, Reading.MORE_VOTES);
    }

    /** "If [option] gets more votes or the vote is tied". */
    public static VoteResult moreVotesOrTied(String option) {
        return new VoteResult(option, Reading.MORE_VOTES_OR_TIED);
    }

    /** "For each [option] vote", done only when there is one. */
    public static VoteResult anyVotes(String option) {
        return new VoteResult(option, Reading.ANY_VOTES);
    }

    @Override
    public String conditionName() {
        return "vote";
    }

    @Override
    public String conditionNotMetReason() {
        String word = option.toLowerCase();
        return switch (reading) {
            case MORE_VOTES -> word + " didn't get more votes";
            case MORE_VOTES_OR_TIED -> word + " didn't get more votes and the vote wasn't tied";
            case ANY_VOTES -> "no " + word + " votes";
        };
    }
}
