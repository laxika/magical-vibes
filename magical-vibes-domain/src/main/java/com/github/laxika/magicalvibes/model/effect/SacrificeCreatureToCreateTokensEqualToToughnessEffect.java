package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Sacrifice a [creature matching filter]. If you do, create X tokens, where X is the
 * sacrificed creature's toughness."
 *
 * <p>At resolution the controller chooses a creature they control that matches {@code filter}
 * to sacrifice. Its effective toughness (with static bonuses) is captured before it leaves
 * the battlefield, then that many copies of {@code tokenTemplate} are created for the
 * controller. The {@code amount} on {@code tokenTemplate} is ignored — the count is always
 * the sacrificed creature's toughness. If the controller has no matching creature, nothing
 * happens.
 *
 * <p>Wrap in a {@link MayEffect} for the "you may sacrifice" optional case (e.g. Feed the Pack).
 *
 * @param tokenTemplate the token to create (power/toughness/color/subtypes/etc.); its amount is ignored
 * @param filter        predicate restricting which creatures may be sacrificed
 */
public record SacrificeCreatureToCreateTokensEqualToToughnessEffect(
        CreateTokenEffect tokenTemplate,
        PermanentPredicate filter
) implements CardEffect {
}
