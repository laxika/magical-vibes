package com.github.laxika.magicalvibes.model.amount;

import java.util.Objects;

/**
 * "For each [option] vote", "the number of truth votes": how many votes the most recent vote of the resolving entry
 * ({@code VoteEffect}, its VOTE step) got for {@code option}, an additional vote included (CR 701.38d). 0 when the entry
 * had no vote, and outside a resolution.
 *
 * @param option the option counted
 */
public record VotesFor(String option) implements DynamicAmount {

    public VotesFor {
        Objects.requireNonNull(option, "option");
    }
}
