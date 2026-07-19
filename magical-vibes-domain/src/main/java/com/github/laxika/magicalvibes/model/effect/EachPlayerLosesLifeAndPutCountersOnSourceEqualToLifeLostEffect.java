package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player loses {@code lifeLossPerPlayer} life. Put a +1/+1 counter on this creature for each 1
 * life lost this way." (Blood Tyrant, UPKEEP_TRIGGERED.)
 *
 * <p>Every player loses the fixed amount, the total life <em>actually</em> lost is summed (a player
 * whose life can't change contributes nothing — CR "this way"), and that many +1/+1 counters are
 * placed on the source permanent. Life loss is not damage (CR 118.2) and feeds "loses life" triggers.
 *
 * @param lifeLossPerPlayer amount of life each player loses
 */
public record EachPlayerLosesLifeAndPutCountersOnSourceEqualToLifeLostEffect(int lifeLossPerPlayer)
        implements CardEffect {
}
