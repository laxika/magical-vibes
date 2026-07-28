package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile this permanent." Immediately exiles the source permanent as part of a resolution — the
 * exile sibling of {@link SacrificeSelfEffect} and the immediate counterpart of
 * {@link ExileSelfAtEndStepEffect}.
 *
 * <p>Typically used as the then-effect of {@link ExileTargetPermanentThenEffect} for "Exile this
 * creature and target creature …" abilities (Giant Trap Door Spider), where the self-exile must be
 * part of the ability's resolution rather than a cost: if the target is illegal on resolution the
 * whole ability is countered and the source stays on the battlefield.
 */
public record ExileSelfEffect() implements CardEffect {
}
