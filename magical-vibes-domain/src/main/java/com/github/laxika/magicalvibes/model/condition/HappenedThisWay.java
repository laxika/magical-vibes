package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
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
 * ({@code minimum} is then 1). A permanent a sacrifice, destroy or exile step removed is judged as it last existed on the
 * battlefield, with its layered characteristics (CR 608.2h).
 *
 * <p>{@code cardFilter} is "if it was a creature card", "when you exile a nonland card this way", "if a land card is
 * discarded this way" ({@link #toACard}): one of the cards an {@link StepKind#EXILE} step exiled from a graveyard, a
 * library or a hand, or a {@link StepKind#DISCARD} step discarded, matches it, judged as the card is now wherever it
 * went (a card keeps its characteristics in a graveyard or exile; CR 608.2h); a permanent an exile step exiled as the
 * card it became in exile ("if the exiled card is a God card", Deicide). A milled or chosen card is judged the same
 * way ("if a creature card was milled this way"), and so is a revealed card ("if no creature cards were revealed this
 * way") and a card a {@link StepKind#RETURN} step returned to a hand ("if it's a Zombie card", Cemetery Recruitment); a
 * permanent a return step put onto the battlefield is judged as a permanent ({@link #toA}).
 *
 * @param kind       the kind of step read
 * @param minimum    the least amount the step must have recorded; at least 1
 * @param filter     a permanent the step recorded must match it; {@code null} for any outcome
 * @param cardFilter a card the step exiled or discarded must match it; {@code null} for any outcome
 */
public record HappenedThisWay(StepKind kind, int minimum, PermanentPredicate filter, CardPredicate cardFilter)
        implements Condition {

    public HappenedThisWay {
        Objects.requireNonNull(kind, "kind");
        if (minimum < 1) {
            throw new IllegalArgumentException("A step happened if it did at least 1: " + minimum);
        }
        if ((filter != null || cardFilter != null) && minimum != 1) {
            throw new IllegalArgumentException("A filtered step result counts no minimum: " + minimum);
        }
        if (filter != null && cardFilter != null) {
            throw new IllegalArgumentException("A step result is filtered as permanents or as cards, not both");
        }
        if (cardFilter != null && kind != StepKind.EXILE && kind != StepKind.DISCARD && kind != StepKind.MILL
                && kind != StepKind.CHOOSE && kind != StepKind.REVEAL && kind != StepKind.RETURN) {
            throw new IllegalArgumentException(
                    "Only an exile, discard, mill, choose, reveal or return step's cards are judged by a card filter: "
                            + kind);
        }
    }

    public HappenedThisWay(StepKind kind, int minimum, PermanentPredicate filter) {
        this(kind, minimum, filter, null);
    }

    public HappenedThisWay(StepKind kind, int minimum) {
        this(kind, minimum, null);
    }

    /**
     * "If it was a creature card", "if a creature card was exiled this way", "when you exile a nonland card this way",
     * "if a creature card was discarded this way": the most recent {@code kind} step (an exile or discard step) exiled
     * or discarded a card that matches {@code filter}.
     */
    public static HappenedThisWay toACard(StepKind kind, CardPredicate filter) {
        return new HappenedThisWay(kind, 1, null, Objects.requireNonNull(filter, "filter"));
    }

    /**
     * "If a Dinosaur is dealt damage this way", "if the sacrificed creature was a Hamster": the most recent
     * {@code kind} step recorded a permanent that, on the battlefield now or as a sacrificed, destroyed or returned
     * one last existed, matches {@code filter} ("if another Desert was returned this way", Arid Archway).
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

    /** "If you do" after a discard step: it discarded a card. */
    public static HappenedThisWay discarded() {
        return new HappenedThisWay(StepKind.DISCARD);
    }

    /** "If you do" after a draw step: it drew a card. */
    public static HappenedThisWay drew() {
        return new HappenedThisWay(StepKind.DRAW);
    }

    /** "If you do" / "if it was sacrificed this way" after a sacrifice step. */
    public static HappenedThisWay sacrificed() {
        return new HappenedThisWay(StepKind.SACRIFICE);
    }

    /** "Remove a counter from it. If you do": the most recent remove-counters step removed at least one counter. */
    public static HappenedThisWay removedCounters() {
        return new HappenedThisWay(StepKind.REMOVE_COUNTERS);
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

    /** "Transform [convert] this. If you do / When you do" after a transform step: the source transformed. */
    public static HappenedThisWay transformed() {
        return new HappenedThisWay(StepKind.TRANSFORM);
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
            case MANIFEST -> "manifested";
            case TRANSFORM -> "transformed";
            case PUT_INTO_GRAVEYARD -> "put into a graveyard";
            case SEEK -> "sought";
            case CHOOSE -> "chosen";
            case REVEAL -> "revealed";
            case PAY_MANA -> "paid";
            case SHUFFLE_INTO_LIBRARY -> "shuffled into a library";
            case PUT_INTO_LIBRARY -> "put into a library";
            case NOTE -> "noted";
            case GAIN_CONTROL -> "taken over";
        };
    }
}
