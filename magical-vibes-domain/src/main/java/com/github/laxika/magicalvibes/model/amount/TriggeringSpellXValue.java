package com.github.laxika.magicalvibes.model.amount;

/**
 * The announced X value of the spell that caused a spell-cast trigger.
 *
 * <p>It reads the stack entry's X, which the spell-cast matcher records as the triggering spell's X
 * (its mana value instead when the ability's target filter reads "that spell's mana value", Skyfire
 * Kirin). Unlike {@link XValue}, it is not reported by {@code AmountEvaluationService.referencesXValue},
 * which asks whether an amount reads the X chosen for the effect's own spell.</p>
 */
public record TriggeringSpellXValue() implements DynamicAmount {
}
