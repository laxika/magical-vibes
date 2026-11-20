package com.github.laxika.magicalvibes.model.effect;

/**
 * Floating layer-6 grant of a rules restriction that is not an ability of the permanent it applies to
 * (e.g. Display of Dominance's "permanents you control can't be the targets of blue or black spells your
 * opponents control"). Unlike {@link GrantEffectEffect}, the granted effect is not removed by a "loses all
 * abilities" effect, since the permanent is not the one that has the ability — the resolved spell's
 * continuous effect merely restricts what may happen to it. Selection of the affected permanents uses the
 * subject predicate of the wrapping floating effect.
 *
 * @param effect the restriction to apply (read by the targeting services like any granted effect)
 * @param subject  {@link Subjects#target()}; the affected permanents come from the floating effect's subject
 */
public record GrantNonAbilityEffectEffect(CardEffect effect, PermanentSubject subject) implements CardEffect {
}
