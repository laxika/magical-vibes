package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * "If you do", "if [a card is drawn | it was sacrificed | that spell is countered | damage is dealt] this way": the
 * most recent {@code kind} step of the resolving entry did something, as it recorded on the entry's resolution
 * scratchpad ({@code StackEntry.getResolutionResults().outcome(kind)}). The steps run in the order written
 * (CR 608.2c), so the step this reads came before. A step that did nothing (a draw that was replaced, a permanent
 * that regenerated or is indestructible, an uncounterable spell, damage that was prevented) fails the condition, and
 * so does an entry with no such step. A permanent counts wherever it ended up: a creature sacrificed into exile
 * instead of the graveyard was still sacrificed. Use it with a {@code ConditionalEffect} after that step, since it
 * is only meaningful as the entry resolves; "if you don't" is a {@link NotCondition} of it.
 *
 * <p>{@code minimum} is how much the step must have done: 1 for "if you do", the written number for "when you draw
 * four cards this way" ({@link #atLeast}): a step that did less than it was told, a library running out, did not do
 * the written action (The Royal Scions' ruling: fewer than four cards drawn don't trigger its "when you do").
 *
 * <p>{@code filter} is "if a [Dinosaur | nonred permanent] is dealt damage this way", "if you sacrifice an Island this
 * way" ({@link #toA}): one of the permanents the step recorded, as it is on the battlefield now, matches it
 * ({@code minimum} is then 1). A permanent a sacrifice or destroy step removed is judged as it last existed on the
 * battlefield, with its layered characteristics (CR 608.2h).
 *
 * @param kind    the kind of step read
 * @param minimum the least amount the step must have recorded; at least 1
 * @param filter  a permanent the step recorded must match it; {@code null} for any outcome
 */
public record HappenedThisWay(StepKind kind, int minimum, PermanentPredicate filter) implements Condition {

    public HappenedThisWay {
        Objects.requireNonNull(kind, "kind");
        if (minimum < 1) {
            throw new IllegalArgumentException("A step happened if it did at least 1: " + minimum);
        }
        if (filter != null && minimum != 1) {
            throw new IllegalArgumentException("A filtered step result counts no minimum: " + minimum);
        }
    }

    public HappenedThisWay(StepKind kind, int minimum) {
        this(kind, minimum, null);
    }

    /**
     * "If a Dinosaur is dealt damage this way", "if the sacrificed creature was a Hamster": the most recent
     * {@code kind} step recorded a permanent that, on the battlefield now or as a sacrificed or destroyed one last
     * existed, matches {@code filter}.
     */
    public static HappenedThisWay toA(StepKind kind, PermanentPredicate filter) {
        return new HappenedThisWay(kind, 1, Objects.requireNonNull(filter, "filter"));
    }

    /** "If you do": the most recent {@code kind} step did anything. */
    public HappenedThisWay(StepKind kind) {
        this(kind, 1);
    }

    /** "When you mill four cards this way": the most recent {@code kind} step did at least {@code minimum}. */
    public static HappenedThisWay atLeast(StepKind kind, int minimum) {
        return new HappenedThisWay(kind, minimum);
    }

    /** "If you do" after an exile step: it exiled something. */
    public static HappenedThisWay exiled() {
        return new HappenedThisWay(StepKind.EXILE);
    }

    /** "If you do" after a return-to-hand step: it returned something. */
    public static HappenedThisWay returned() {
        return new HappenedThisWay(StepKind.RETURN);
    }

    /** "If you do" after a token step: it created a token. */
    public static HappenedThisWay created() {
        return new HappenedThisWay(StepKind.CREATE_TOKEN);
    }

    /** "If you do" after a draw step: it drew a card. */
    public static HappenedThisWay drew() {
        return new HappenedThisWay(StepKind.DRAW);
    }

    /** "If you do" / "if it was sacrificed this way" after a sacrifice step. */
    public static HappenedThisWay sacrificed() {
        return new HappenedThisWay(StepKind.SACRIFICE);
    }

    /** "If it was destroyed this way" after a destroy step. */
    public static HappenedThisWay destroyed() {
        return new HappenedThisWay(StepKind.DESTROY);
    }

    /** "If that spell is countered this way" after a counter step. */
    public static HappenedThisWay countered() {
        return new HappenedThisWay(StepKind.COUNTER_SPELL);
    }

    /** "If you copy a spell this way"; "if you don't" is {@code new NotCondition(copied())}. */
    public static HappenedThisWay copied() {
        return new HappenedThisWay(StepKind.COPY_SPELL);
    }

    @Override
    public String conditionName() {
        return verb() + " this way";
    }

    @Override
    public String conditionNotMetReason() {
        return "nothing was " + verb() + " this way";
    }

    private String verb() {
        return switch (kind) {
            case DRAW -> "drawn";
            case DISCARD -> "discarded";
            case MILL -> "milled";
            case EXILE -> "exiled";
            case DESTROY -> "destroyed";
            case SACRIFICE -> "sacrificed";
            case RETURN -> "returned";
            case CREATE_TOKEN -> "created";
            case DEAL_DAMAGE -> "dealt damage";
            case GAIN_LIFE -> "gained";
            case LOSE_LIFE -> "lost";
            case PUT_COUNTERS -> "given counters";
            case REMOVE_COUNTERS -> "stripped of counters";
            case COUNTER_SPELL -> "countered";
            case COPY_SPELL -> "copied";
        };
    }
}
