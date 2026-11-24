package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * "Choose a creature an opponent controls", "choose up to two creatures", "choose any number of creatures with
 * different powers": as the effect resolves its controller chooses among the permanents {@code candidates} names
 * (CR 608.2d); none of them is targeted. With no more candidates than must be chosen, all of them are, unasked.
 * "For each opponent, choose a nonland permanent that player controls" is {@link #oneForEach}: one choice for each
 * of those players in turn, among the candidates that player controls. "One of them chosen at random" is
 * {@link #atRandom}: nobody is asked. "Target opponent chooses a creature they control" is {@link #chosenBy}: that player
 * makes the one choice, and this effect declares the player target the chooser names. "Choose three target permanents.
 * Destroy one of them at random" is {@link #atRandom} among {@code Subjects.allTargets()}: the targets are declared by
 * the spell or ability (its {@code target(...)} groups), this effect declares none, and the pick is among those still
 * legal as it resolves (CR 608.2b). "Choose two target creatures controlled by the same player. That player sacrifices
 * one of them" is {@link #exactly} one among {@code Subjects.allTargets()}, {@link #chosenBy} their controller
 * ({@code controllerOf(allTargets())}): with one target left legal, it is chosen unasked. "For each [option] vote, the
 * voter chooses a creature they control" and "for each [option] vote, choose a permanent owned by the voter" are
 * {@link #forEachVote}: one choice per vote of the resolution's last vote ({@code VoteEffect}), in the order the votes
 * were cast.
 *
 * <p>A CHOOSE step that pauses at its end on the choice: {@code Subjects.thisWay(StepKind.CHOOSE)} is what was chosen,
 * for the steps after it. A source that chose one permanent also keeps it as its chosen permanent ("when the chosen
 * creature leaves the battlefield").
 *
 * @param candidates among which permanents; nothing here is targeted
 * @param minimum    how many must be chosen, as far as there are that many
 * @param maximum    how many may be chosen; {@code null} for any number
 * @param different  the quality no two of the chosen share, or {@code null}; only with no minimum
 * @param label      what is chosen, for the prompt ("up to two creatures")
 * @param oneForEach for each of whom one that player controls is chosen; {@code null} for one choice among all
 * @param random     whether they are chosen at random, {@code maximum} of them
 * @param chooser    who makes the one choice; {@code null} for the controller
 * @param forEachVote for each vote of which option one is chosen, and by and about whom; {@code null} for none
 */
public record ChoosePermanentsEffect(PermanentSubject candidates, DynamicAmount minimum, DynamicAmount maximum,
                                     Different different, String label, PlayerSubject oneForEach,
                                     boolean random, PlayerSubject chooser, ForEachVote forEachVote)
        implements CardEffect {

    /**
     * One choice of one candidate for each vote for {@code option} in the most recent vote of the same resolution
     * ({@code StepOutcome.votes()}), in the order the votes were cast (CR 701.38a; a player's several votes together,
     * CR 701.38d), among the candidates that vote's voter controls or owns; the voter makes it when {@code byVoter},
     * else the effect's controller. A choice with one candidate is made unasked (CR 608.2d); the same permanent may be
     * chosen for two votes, and is chosen once.
     *
     * @param option   the word voted for
     * @param relation whether the candidates are those the voter controls or owns
     * @param byVoter  whether the voter chooses ("the voter chooses a creature they control") rather than you
     */
    public record ForEachVote(String option, VoterRelation relation, boolean byVoter) {

        public ForEachVote {
            Objects.requireNonNull(option, "option");
            Objects.requireNonNull(relation, "relation");
        }
    }

    /** Which of the candidates are a voter's: those they control, or those they own. */
    public enum VoterRelation {
        CONTROLS,
        OWNS
    }

    /** "With different powers", "with different mana values". */
    public enum Different {
        POWER("powers"),
        MANA_VALUE("mana values");

        private final String plural;

        Different(String plural) {
            this.plural = plural;
        }

        public String plural() {
            return plural;
        }
    }

    public ChoosePermanentsEffect {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(minimum, "minimum");
        Objects.requireNonNull(label, "label");
        if (candidates.declaresTarget() && !(candidates instanceof PermanentSubject.AllTargets)) {
            throw new IllegalArgumentException("A permanent chosen as the effect resolves is no target: " + candidates);
        }
        if (different != null && !minimum.equals(new Fixed(0))) {
            throw new IllegalArgumentException("Permanents with different qualities are any number: " + minimum);
        }
        if (oneForEach != null && (different != null || random || !minimum.equals(new Fixed(1))
                || !new Fixed(1).equals(maximum) || oneForEach.declaresTarget())) {
            throw new IllegalArgumentException("One is chosen for each of players who are no target: " + oneForEach);
        }
        if (random && (different != null || maximum == null)) {
            throw new IllegalArgumentException("A number of permanents is chosen at random: " + maximum);
        }
        if (chooser != null && (oneForEach != null || random)) {
            throw new IllegalArgumentException("Only the one choice among all is made by another player: " + chooser);
        }
        if (forEachVote != null && (oneForEach != null || chooser != null || different != null || random
                || !minimum.equals(new Fixed(1)) || !new Fixed(1).equals(maximum))) {
            throw new IllegalArgumentException("One is chosen for each vote, by the voter or by you: " + forEachVote);
        }
    }

    public ChoosePermanentsEffect(PermanentSubject candidates, DynamicAmount minimum, DynamicAmount maximum,
                                  Different different, String label, PlayerSubject oneForEach,
                                  boolean random, PlayerSubject chooser) {
        this(candidates, minimum, maximum, different, label, oneForEach, random, chooser, null);
    }

    /** "Target opponent chooses …" declares that player target; the candidates never declare one. */
    @Override
    public TargetSpec targetSpec() {
        return chooser != null && chooser.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    /** The controller's one choice among all the candidates. */
    public ChoosePermanentsEffect(PermanentSubject candidates, DynamicAmount minimum, DynamicAmount maximum,
                                  Different different, String label) {
        this(candidates, minimum, maximum, different, label, null, false, null);
    }

    /** "For each [of players], choose [one of candidates] that player controls." */
    public static ChoosePermanentsEffect oneForEach(PlayerSubject players, PermanentSubject candidates,
                                                    String label) {
        return new ChoosePermanentsEffect(candidates, new Fixed(1), new Fixed(1), null, label,
                Objects.requireNonNull(players, "players"), false, null);
    }

    /**
     * "For each [option] vote, the voter chooses a creature they control" ({@code CONTROLS}, {@code byVoter}), "for each
     * [option] vote, choose a permanent owned by the voter" ({@code OWNS}): see {@link ForEachVote}.
     */
    public static ChoosePermanentsEffect forEachVote(String option, PermanentSubject candidates, VoterRelation relation,
                                                     boolean byVoter, String label) {
        return new ChoosePermanentsEffect(candidates, new Fixed(1), new Fixed(1), null, label, null, false, null,
                new ForEachVote(option, relation, byVoter));
    }

    /** "[Count] of [candidates] chosen at random." */
    public static ChoosePermanentsEffect atRandom(int count, PermanentSubject candidates, String label) {
        return new ChoosePermanentsEffect(candidates, new Fixed(count), new Fixed(count), null, label, null, true,
                null);
    }

    /** "Choose [count] [candidates]." */
    public static ChoosePermanentsEffect exactly(int count, PermanentSubject candidates, String label) {
        return new ChoosePermanentsEffect(candidates, new Fixed(count), new Fixed(count), null, label);
    }

    /** "Choose up to [count] [candidates]." */
    public static ChoosePermanentsEffect upTo(int count, PermanentSubject candidates, String label) {
        return new ChoosePermanentsEffect(candidates, new Fixed(0), new Fixed(count), null, label);
    }

    /** "Choose any number of [candidates]." */
    public static ChoosePermanentsEffect anyNumber(PermanentSubject candidates, String label) {
        return new ChoosePermanentsEffect(candidates, new Fixed(0), null, null, label);
    }

    /** These, no two with the same {@code quality}. */
    public ChoosePermanentsEffect withDifferent(Different quality) {
        return new ChoosePermanentsEffect(candidates, minimum, maximum, quality,
                label + " with different " + quality.plural(), oneForEach, random, chooser, forEachVote);
    }

    /** The same choice made by {@code player} ("target opponent chooses a creature they control"). */
    public ChoosePermanentsEffect chosenBy(PlayerSubject player) {
        return new ChoosePermanentsEffect(candidates, minimum, maximum, different, label, oneForEach, random,
                Objects.requireNonNull(player, "player"), forEachVote);
    }

    /** Who makes the choice. */
    public PlayerSubject choosingPlayer() {
        return chooser == null ? Subjects.you() : chooser;
    }
}
