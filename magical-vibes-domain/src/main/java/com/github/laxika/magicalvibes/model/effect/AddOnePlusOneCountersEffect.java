package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A static counter-placement replacement (CR 614.1a): "if one or more [kind] counters would be put on [permanents] [or
 * players] / if [you | an opponent] would put …, [that many plus one | twice that many | half that many, rounded down]
 * of each of those kinds … instead". Hardened Scales is the no-argument form.
 *
 * <p>Each applies once to a placement (CR 614.5); when several apply, the affected object's controller or the affected
 * player orders them (CR 616.1), asked by the placement paths of {@code GameQueryService} where the order changes the
 * result.
 *
 * @param counterType the kind it replaces, {@link CounterType#ANY} for every kind ("of each of those kinds")
 * @param permanents  the permanents whose counters it replaces, judged against its source (the predicate's "you" is
 *                    the source's controller: {@link #youControl()}, "this permanent" is
 *                    {@link PermanentIsSourceCardPredicate}), or {@code null} for none
 * @param players     the players whose counters it replaces: {@code null} for none, {@link PlayerRelation#SELF} for "you
 *                    would get", {@link PlayerRelation#ANY} for "on a permanent or player"
 * @param placedBy    who must be putting the counters: {@link PlayerRelation#ANY} ("would be put on"),
 *                    {@link PlayerRelation#SELF} ("if you would put"), {@link PlayerRelation#OPPONENT} ("if an opponent
 *                    would put"); a placement that names no player is put by the affected object's controller
 * @param change      the replaced number
 */
public record AddOnePlusOneCountersEffect(CounterType counterType, PermanentPredicate permanents,
                                          PlayerRelation players, PlayerRelation placedBy, Change change)
        implements CounterReplacementEffect, DoublingEffect {

    /** How the number of counters is replaced. */
    public enum Change {
        /** "that many plus one". */
        PLUS_ONE,
        /** "twice that many". */
        TWICE,
        /** "half that many, rounded down". */
        HALF_ROUNDED_DOWN
    }

    public AddOnePlusOneCountersEffect {
        Objects.requireNonNull(counterType, "counterType");
        Objects.requireNonNull(placedBy, "placedBy");
        Objects.requireNonNull(change, "change");
        if (players == PlayerRelation.OPPONENT) {
            throw new IllegalArgumentException("players is SELF, ANY or null");
        }
    }

    /** Hardened Scales: "If one or more +1/+1 counters would be put on a creature you control, that many plus one". */
    public AddOnePlusOneCountersEffect() {
        this(CounterType.PLUS_ONE_PLUS_ONE, youControl(new PermanentIsCreaturePredicate()), null, PlayerRelation.ANY,
                Change.PLUS_ONE);
    }

    /** "If one or more [kind] counters would be put on [permanents], that many plus one … are put on it instead." */
    public static AddOnePlusOneCountersEffect plusOne(CounterType counterType, PermanentPredicate permanents) {
        return new AddOnePlusOneCountersEffect(counterType, permanents, null, PlayerRelation.ANY, Change.PLUS_ONE);
    }

    /** "If you would get one or more counters, you get that many plus one of each of those kinds of counters instead." */
    public static AddOnePlusOneCountersEffect plusOneForYou() {
        return new AddOnePlusOneCountersEffect(CounterType.ANY, null, PlayerRelation.SELF, PlayerRelation.ANY,
                Change.PLUS_ONE);
    }

    /** "If one or more [kind] counters would be put on [permanents], twice that many … are put on it instead." */
    public static AddOnePlusOneCountersEffect twice(CounterType counterType, PermanentPredicate permanents) {
        return new AddOnePlusOneCountersEffect(counterType, permanents, null, PlayerRelation.ANY, Change.TWICE);
    }

    /** "… half that many of each of those kinds of counters instead, rounded down." */
    public static AddOnePlusOneCountersEffect halfRoundedDown(CounterType counterType, PermanentPredicate permanents) {
        return new AddOnePlusOneCountersEffect(counterType, permanents, null, PlayerRelation.ANY,
                Change.HALF_ROUNDED_DOWN);
    }

    /** "A permanent you control": the source's controller controls it. */
    public static PermanentPredicate youControl() {
        return new PermanentControlledBySourceControllerPredicate();
    }

    /** "A [filter] you control". */
    public static PermanentPredicate youControl(PermanentPredicate filter) {
        return new PermanentAllOfPredicate(List.of(new PermanentControlledBySourceControllerPredicate(), filter));
    }

    /** "A creature, [subtype] or [subtype]": a creature or a permanent with any of {@code subtypes}. */
    public static PermanentPredicate creatureOr(CardSubtype... subtypes) {
        return new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentHasAnySubtypePredicate(Set.of(subtypes))));
    }

    /** "A permanent": any permanent, whoever controls it. */
    public static PermanentPredicate anyPermanent() {
        return new PermanentTruePredicate();
    }

    /** "If you would put": only counters the source's controller puts. */
    public AddOnePlusOneCountersEffect placedByYou() {
        return new AddOnePlusOneCountersEffect(counterType, permanents, players, PlayerRelation.SELF, change);
    }

    /** "If an opponent would put": only counters an opponent of the source's controller puts. */
    public AddOnePlusOneCountersEffect placedByAnOpponent() {
        return new AddOnePlusOneCountersEffect(counterType, permanents, players, PlayerRelation.OPPONENT, change);
    }

    /** "On a permanent or player": the counters any player would get too. */
    public AddOnePlusOneCountersEffect alsoOnPlayers() {
        return new AddOnePlusOneCountersEffect(counterType, permanents, PlayerRelation.ANY, placedBy, change);
    }

    /** Whether it replaces counters of {@code kind} ({@code null}: a player's counters, which have no kind here). */
    public boolean replacesKind(CounterType kind) {
        return counterType == CounterType.ANY || counterType == kind;
    }

    /**
     * Whether counters put by {@code placingPlayerId} meet {@link #placedBy()} for a source controlled by
     * {@code sourceControllerId}; {@code null} for a placement whose placer is not known, which only
     * {@link PlayerRelation#ANY} accepts.
     */
    public boolean acceptsPlacer(UUID sourceControllerId, UUID placingPlayerId) {
        return switch (placedBy) {
            case ANY -> true;
            case SELF -> placingPlayerId != null && placingPlayerId.equals(sourceControllerId);
            case OPPONENT -> placingPlayerId != null && sourceControllerId != null
                    && !placingPlayerId.equals(sourceControllerId);
        };
    }

    /** Whether the counters {@code affectedPlayerId} would get are among the players it replaces. */
    public boolean replacesPlayer(UUID sourceControllerId, UUID affectedPlayerId) {
        if (players == null || affectedPlayerId == null) {
            return false;
        }
        return players == PlayerRelation.ANY || affectedPlayerId.equals(sourceControllerId);
    }

    /**
     * Whether it affects only its own source ("put on Mowu"), and so applies to the counters its source enters with
     * (Mowu's ruling), unlike a "creature you control" replacement, which doesn't apply to itself entering.
     */
    public boolean affectsOnlyItsSource() {
        return permanents instanceof PermanentIsSourceCardPredicate;
    }

    @Override
    public int replace(CounterType counterType, int count) {
        if (count <= 0) {
            return count;
        }
        return switch (change) {
            case PLUS_ONE -> SaturatingMath.add(count, 1);
            case TWICE -> SaturatingMath.multiply(count, 2);
            case HALF_ROUNDED_DOWN -> count / 2;
        };
    }

    @Override
    public boolean isDoublingEffect() {
        return change == Change.TWICE;
    }
}
