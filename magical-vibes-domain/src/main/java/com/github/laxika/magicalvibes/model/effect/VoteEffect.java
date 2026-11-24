package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * "Starting with you, each player votes for time or knowledge", "each player secretly votes for truth or consequences,
 * then those votes are revealed", "starting with you, each player votes for a nonland permanent you don't control":
 * a vote (CR 701.38a). The choices are words with no rules meaning, objects, or players (CR 701.38b): the
 * {@link Candidates}. Each player, starting with {@code firstVoter} and in turn order, chooses one of them; a player who
 * may vote an additional time ({@code MayVoteAdditionalTimeEffect}) casts those votes when they would otherwise vote
 * (CR 701.38d). An open vote is logged as it is cast, so each player knows the votes cast before theirs; a secret vote
 * is shown to nobody until the last one is cast, then all of them are revealed together.
 *
 * <p>A vote for objects or players offers each voter the candidates found as the vote begins, those that can't be
 * chosen left out; a voter with no candidate doesn't vote, and a voter with one candidate and no additional vote votes
 * for it unasked (CR 608.2d). {@code upToOne} is "votes for up to one creature": a voter may cast no vote.
 *
 * <p>A VOTE step that pauses at its end: it records each vote ({@code StepOutcome.votes()}) as the last one is cast and
 * the resolution goes on. The steps after it read the result: "if [option] gets more votes" / "if [option] gets more
 * votes or the vote is tied" is a {@code ConditionalEffect} of {@code VoteResult}, "for each [option] vote" a
 * {@code VotesFor} amount; "each permanent with the most votes or tied for most votes" is {@code Subjects.mostVoted()},
 * "each creature with one or more votes" {@code Subjects.votedFor()}, "each card with the most votes"
 * {@code Subjects.mostVotedCards()}, "each player who received no votes" {@code Subjects.playersWithNoVotes()}, and
 * "that many" / "for each vote they received" a {@code VotesReceived} amount. Finishing the vote is the "players
 * finish voting" event (Erestor of the Council).
 *
 * @param candidates what is voted for
 * @param firstVoter who votes first; the others follow in turn order
 * @param secret     whether the votes are cast secretly and revealed together
 * @param upToOne    whether a voter may cast no vote ("votes for up to one creature"); only for objects and players
 */
public record VoteEffect(Candidates candidates, PlayerSubject firstVoter, boolean secret, boolean upToOne)
        implements CardEffect {

    /** What a vote is for (CR 701.38b). */
    public sealed interface Candidates permits Words, Permanents, Cards, Players {
    }

    /**
     * Words with no rules meaning, each connected to a different effect, in the order they are offered.
     *
     * @param options the words
     */
    public record Words(List<String> options) implements Candidates {

        public Words {
            options = List.copyOf(options);
            if (options.size() < 2 || options.stream().distinct().count() != options.size()) {
                throw new IllegalArgumentException("A vote is among two or more different options: " + options);
            }
        }
    }

    /**
     * Permanents: "a nonland permanent you don't control" is {@code controlledBy(eachOpponent(), nonland)}.
     *
     * @param permanents the permanents voted among, found as the vote begins
     */
    public record Permanents(PermanentSubject permanents) implements Candidates {

        public Permanents {
            Objects.requireNonNull(permanents, "permanents");
            if (permanents.declaresTarget()) {
                throw new IllegalArgumentException("The candidates of a vote are not targeted: " + permanents);
            }
        }
    }

    /**
     * Cards outside the battlefield: "an artifact, creature, or enchantment card in your graveyard".
     *
     * @param cards the cards voted among, found as the vote begins; only in a public zone
     */
    public record Cards(CardSubject cards) implements Candidates {

        public Cards {
            Objects.requireNonNull(cards, "cards");
            if (cards.declaresTarget() || cards.inHiddenZone()) {
                throw new IllegalArgumentException("The candidates of a vote are untargeted cards in a public zone: "
                        + cards);
            }
        }
    }

    /**
     * Players: "votes for a player" is {@code eachPlayer()}; "votes for another player" ({@code otherThanVoter}) leaves
     * each voter out of their own candidates.
     *
     * @param players        the players voted among, found as the vote begins
     * @param otherThanVoter whether a voter may not vote for themselves
     */
    public record Players(PlayerSubject players, boolean otherThanVoter) implements Candidates {

        public Players {
            Objects.requireNonNull(players, "players");
            if (players.declaresTarget()) {
                throw new IllegalArgumentException("The candidates of a vote are not targeted: " + players);
            }
        }
    }

    /** Which candidates of a vote for objects or players a reader names, by the votes each received. */
    public enum Tally {
        /** "With the most votes or tied for most votes": the most votes any candidate received, at least one. */
        MOST,
        /** "With one or more votes". */
        ANY,
        /** "Who received no votes" (players only: every player in the game no vote named). */
        NONE
    }

    public VoteEffect {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(firstVoter, "firstVoter");
        if (firstVoter.declaresTarget()) {
            throw new IllegalArgumentException("The first voter is no target: " + firstVoter);
        }
        if (upToOne && candidates instanceof Words) {
            throw new IllegalArgumentException("A vote among words has no abstention");
        }
    }

    /** A vote among words, open or secret, every vote cast. */
    public VoteEffect(List<String> options, PlayerSubject firstVoter, boolean secret) {
        this(new Words(options), firstVoter, secret, false);
    }

    /** The words voted for, or no words for a vote among objects or players. */
    public List<String> options() {
        return candidates instanceof Words words ? words.options() : List.of();
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

    /** "Starting with you, each player votes for [a permanent]": an open vote among {@code permanents}. */
    public static VoteEffect forPermanents(PermanentSubject permanents) {
        return new VoteEffect(new Permanents(permanents), Subjects.you(), false, false);
    }

    /** "Starting with you, each player votes for [a card]": an open vote among {@code cards}. */
    public static VoteEffect forCards(CardSubject cards) {
        return new VoteEffect(new Cards(cards), Subjects.you(), false, false);
    }

    /** "Each player votes for a player": a vote among the players {@code players} names, you voting first. */
    public static VoteEffect forPlayers(PlayerSubject players) {
        return new VoteEffect(new Players(players, false), Subjects.you(), false, false);
    }

    /** "Each player votes for another player", you voting first. */
    public static VoteEffect forAnotherPlayer() {
        return new VoteEffect(new Players(Subjects.eachPlayer(), true), Subjects.you(), false, false);
    }

    /** This vote cast secretly, its votes revealed together after the last one; the first voter is kept. */
    public VoteEffect castSecretly() {
        return new VoteEffect(candidates, firstVoter, true, upToOne);
    }

    /** This vote with "up to one": a voter may cast no vote. */
    public VoteEffect forUpToOne() {
        return new VoteEffect(candidates, firstVoter, secret, true);
    }
}
