package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * "Starting with you, each player votes for time or knowledge", "each player secretly votes for truth or consequences,
 * then those votes are revealed": a vote among words (CR 701.38a, 701.38b). Each player, starting with
 * {@code firstVoter} and in turn order, chooses one of {@code options}; a player who may vote an additional time
 * ({@code MayVoteAdditionalTimeEffect}) casts those votes when they would otherwise vote (CR 701.38d). An open vote is
 * logged as it is cast, so each player knows the votes cast before theirs; a secret vote is shown to nobody until the
 * last one is cast, then all of them are revealed together.
 *
 * <p>A VOTE step that pauses at its end: it records each vote ({@code StepOutcome.votes()}) as the last one is cast and
 * the resolution goes on. The steps after it read the result: "if [option] gets more votes" / "if [option] gets more
 * votes or the vote is tied" is a {@code ConditionalEffect} of {@code VoteResult}, "for each [option] vote" a
 * {@code VotesFor} amount. Finishing the vote is the "players finish voting" event (Erestor of the Council).
 *
 * @param options    the words voted for, in the order they are offered
 * @param firstVoter who votes first; the others follow in turn order
 * @param secret     whether the votes are cast secretly and revealed together
 */
public record VoteEffect(List<String> options, PlayerSubject firstVoter, boolean secret) implements CardEffect {

    public VoteEffect {
        options = List.copyOf(options);
        Objects.requireNonNull(firstVoter, "firstVoter");
        if (options.size() < 2 || options.stream().distinct().count() != options.size()) {
            throw new IllegalArgumentException("A vote is among two or more different options: " + options);
        }
        if (firstVoter.declaresTarget()) {
            throw new IllegalArgumentException("The first voter is no target: " + firstVoter);
        }
    }

    /** "Starting with you, each player votes for [options]". */
    public static VoteEffect startingWithYou(String... options) {
        return new VoteEffect(List.of(options), Subjects.you(), false);
    }

    /**
     * "Each player secretly votes for [options], then those votes are revealed": the players choose as players making
     * choices at the same time do, the active player first (CR 101.4).
     */
    public static VoteEffect secretly(String... options) {
        return new VoteEffect(List.of(options), Subjects.activePlayer(), true);
    }
}
