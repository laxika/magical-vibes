package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player sacrifices the permanents they control matching {@code filter} down to the number
 * controlled by the player who controls the fewest such permanents. Each player chooses which of
 * their own matching permanents to keep and sacrifices the rest. Uses the APNAP simultaneous forced
 * sacrifice flow (CR 101.4): every player chooses in turn order, then all chosen permanents are
 * sacrificed at the same time.
 *
 * <p>Example: {@code new EachPlayerSacrificesDownToFewestEffect(new PermanentIsLandPredicate())} →
 * each player keeps a number of lands equal to the fewest any player controls and sacrifices the
 * rest (Balance).
 */
public record EachPlayerSacrificesDownToFewestEffect(PermanentPredicate filter) implements CardEffect {
}
