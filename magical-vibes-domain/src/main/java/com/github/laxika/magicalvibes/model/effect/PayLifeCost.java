package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect: pay life to activate an ability.
 * When {@code halfLifeRoundedUp} is true, the amount is instead "half your life, rounded up",
 * computed from the current life total at activation time (e.g. Murderous Betrayal).
 * Validated and paid during ability activation in AbilityActivationService.
 */
public record PayLifeCost(int amount, boolean halfLifeRoundedUp) implements CostEffect {

    public PayLifeCost(int amount) {
        this(amount, false);
    }

    /** "Pay half your life, rounded up." */
    public static PayLifeCost halfLife() {
        return new PayLifeCost(0, true);
    }

    /** The life actually paid given the player's current life total. */
    public int effectiveAmount(int currentLife) {
        return halfLifeRoundedUp ? (currentLife + 1) / 2 : amount;
    }

    @Override
    public int lifePaid(int currentLife) {
        return effectiveAmount(currentLife);
    }
}
