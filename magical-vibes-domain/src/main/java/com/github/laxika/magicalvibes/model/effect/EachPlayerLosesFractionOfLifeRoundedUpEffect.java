package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player loses 1/divisor of their life total, rounded up.
 * Example: divisor=3 → each player loses a third of their life, rounded up.
 */
public record EachPlayerLosesFractionOfLifeRoundedUpEffect(int divisor, FractionalLifeLossRecipient recipient)
        implements CardEffect {

    public EachPlayerLosesFractionOfLifeRoundedUpEffect(int divisor, boolean opponentsOnly) {
        this(divisor, opponentsOnly ? FractionalLifeLossRecipient.OPPONENTS : FractionalLifeLossRecipient.ALL);
    }

    public boolean opponentsOnly() { return recipient == FractionalLifeLossRecipient.OPPONENTS; }

    public EachPlayerLosesFractionOfLifeRoundedUpEffect(int divisor) {
        this(divisor, false);
    }

    public static EachPlayerLosesFractionOfLifeRoundedUpEffect opponentsOnly(int divisor) {
        return new EachPlayerLosesFractionOfLifeRoundedUpEffect(divisor, true);
    }
}
