package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.StepKind;

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
 */
public record HappenedThisWay(StepKind kind) implements Condition {

    public HappenedThisWay {
        Objects.requireNonNull(kind, "kind");
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
