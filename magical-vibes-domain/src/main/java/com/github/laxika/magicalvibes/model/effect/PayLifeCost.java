package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Cost effect: pay life to activate an ability.
 * When {@code halfLifeRoundedUp} is true, the amount is instead "half your life, rounded up",
 * computed from the current life total at activation time (e.g. Murderous Betrayal).
 * When {@code perSourceCounter} is non-null, {@code amount} is paid once for each counter of that
 * type on the source permanent — "Pay 3 life for each velocity counter on this enchantment"
 * (Tornado), so an activation with no such counters costs no life at all.
 * Validated and paid during ability activation in AbilityActivationService.
 */
public record PayLifeCost(int amount, boolean halfLifeRoundedUp, CounterType perSourceCounter)
        implements CostEffect {

    public PayLifeCost(int amount) {
        this(amount, false, null);
    }

    public PayLifeCost(int amount, boolean halfLifeRoundedUp) {
        this(amount, halfLifeRoundedUp, null);
    }

    /** "Pay half your life, rounded up." */
    public static PayLifeCost halfLife() {
        return new PayLifeCost(0, true, null);
    }

    /** "Pay {@code amount} life for each {@code counterType} counter on this permanent." */
    public static PayLifeCost perSourceCounter(int amount, CounterType counterType) {
        return new PayLifeCost(amount, false, counterType);
    }

    /** The life actually paid given the player's current life total. */
    public int effectiveAmount(int currentLife) {
        return effectiveAmount(currentLife, 0);
    }

    /**
     * The life actually paid given the player's current life total and the number of
     * {@link #perSourceCounter} counters on the source permanent (ignored when that field is null).
     */
    public int effectiveAmount(int currentLife, int sourceCounterCount) {
        if (halfLifeRoundedUp) {
            return (currentLife + 1) / 2;
        }
        return perSourceCounter != null ? amount * sourceCounterCount : amount;
    }

    @Override
    public int lifePaid(int currentLife) {
        return effectiveAmount(currentLife);
    }
}
