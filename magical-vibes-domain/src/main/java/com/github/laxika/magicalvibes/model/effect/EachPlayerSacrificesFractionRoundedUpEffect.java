package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player sacrifices 1/divisor of the permanents they control matching {@code filter}, rounded
 * up, chosen by that player. The count is recomputed per player against their own matching
 * permanents, so each player sacrifices a different number. Uses the APNAP simultaneous forced
 * sacrifice flow (CR 101.4): every player chooses in turn order, then all chosen permanents are
 * sacrificed at the same time.
 *
 * <p>Example: {@code new EachPlayerSacrificesFractionRoundedUpEffect(3, new PermanentIsCreaturePredicate())}
 * → each player sacrifices a third of the creatures they control, rounded up (Pox).
 */
public record EachPlayerSacrificesFractionRoundedUpEffect(int divisor, PermanentPredicate filter)
        implements CardEffect {
}
