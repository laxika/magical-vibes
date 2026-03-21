package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that resolves its inner effect only when the attacking
 * creature count meets or exceeds a minimum threshold.
 * <p>
 * Used for graveyard-triggered abilities that fire "whenever you attack with
 * N or more creatures" (e.g. Warcry Phoenix). The attacker count is passed
 * via {@code xValue} on the stack entry, locked at trigger time.
 * <p>
 * The condition is checked at trigger time in {@code CombatAttackService}
 * (the trigger is not pushed if the minimum is not met) and re-checked at
 * resolution time via {@code EffectResolutionService.isConditionalMet()}.
 *
 * @param minimumAttackers the minimum number of attacking creatures required
 * @param wrapped          the inner effect to resolve when the condition is met
 */
public record MinimumAttackersConditionalEffect(int minimumAttackers, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return minimumAttackers + " or more attackers";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimumAttackers + " creatures attacking";
    }
}
