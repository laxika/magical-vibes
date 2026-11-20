package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * What one resolution step of a {@link StepKind} did: how much, to which objects, for which players. What each kind
 * puts in each component is listed on {@link StepKind}. Immutable, so a {@link StackEntry} copy shares it.
 *
 * @param amount    how many cards, permanents, counters or tokens, or how much damage or life; never negative
 * @param objectIds the cards or permanents the step acted on, in the order it acted
 * @param playerIds the players the step happened to, or who controlled or owned what it acted on (CR 608.2h), each
 *                  once, in the order the step reached them
 */
public record StepOutcome(int amount, List<UUID> objectIds, List<UUID> playerIds) {

    /** A step that did nothing. */
    public static final StepOutcome NONE = new StepOutcome(0, List.of(), List.of());

    public StepOutcome {
        if (amount < 0) {
            throw new IllegalArgumentException("A step outcome's amount is not negative: " + amount);
        }
        objectIds = List.copyOf(objectIds);
        playerIds = List.copyOf(new LinkedHashSet<>(playerIds));
    }

    /** An outcome of {@code objectIds.size()} objects. */
    public static StepOutcome ofObjects(List<UUID> objectIds, List<UUID> playerIds) {
        return new StepOutcome(objectIds.size(), objectIds, playerIds);
    }

    /** An outcome that counts {@code amount} for players and acted on no object (life gained or lost). */
    public static StepOutcome ofAmount(int amount, List<UUID> playerIds) {
        return new StepOutcome(Math.max(0, amount), List.of(), playerIds);
    }

    /** Whether the step did anything ("if you do", "this way"). */
    public boolean happened() {
        return amount > 0;
    }

    /** This outcome with {@code more} added: amounts summed, objects and players appended. */
    public StepOutcome plus(StepOutcome more) {
        List<UUID> objects = new ArrayList<>(objectIds);
        objects.addAll(more.objectIds);
        List<UUID> players = new ArrayList<>(playerIds);
        players.addAll(more.playerIds);
        return new StepOutcome(SaturatingMath.add(amount, more.amount), objects, players);
    }
}
