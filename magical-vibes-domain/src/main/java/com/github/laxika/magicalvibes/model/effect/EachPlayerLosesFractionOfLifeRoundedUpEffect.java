package com.github.laxika.magicalvibes.model.effect;

/**
 * Each named player loses 1/divisor of their life total, rounded up; every loss is computed from the life totals
 * before any is applied, and the losses happen at once.
 * Example: divisor=3 → each player loses a third of their life, rounded up.
 *
 * @param divisor the fraction's denominator
 * @param player  who loses life; declares no target
 */
public record EachPlayerLosesFractionOfLifeRoundedUpEffect(int divisor, PlayerSubject player)
        implements CardEffect {

    /** Each player loses the fraction. */
    public EachPlayerLosesFractionOfLifeRoundedUpEffect(int divisor) {
        this(divisor, Subjects.eachPlayer());
    }

    public EachPlayerLosesFractionOfLifeRoundedUpEffect {
        if (player.declaresTarget()) {
            throw new IllegalArgumentException("A fractional life loss declares no target: " + player);
        }
    }

    /** Each opponent loses the fraction. */
    public static EachPlayerLosesFractionOfLifeRoundedUpEffect opponentsOnly(int divisor) {
        return new EachPlayerLosesFractionOfLifeRoundedUpEffect(divisor, Subjects.eachOpponent());
    }
}
