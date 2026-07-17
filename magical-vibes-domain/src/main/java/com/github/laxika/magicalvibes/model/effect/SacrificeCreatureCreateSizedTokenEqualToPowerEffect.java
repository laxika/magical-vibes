package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Sacrifice a [creature matching filter]. If you do, create a single token whose power and
 * toughness are each equal to the sacrificed creature's power."
 *
 * <p>At resolution the controller chooses a creature they control that matches {@code filter}
 * to sacrifice. Its effective power (with static bonuses) is captured before it leaves the
 * battlefield, then one copy of {@code tokenTemplate} is created for the controller with its
 * power and toughness overridden to that value. The {@code power}/{@code toughness}/{@code amount}
 * on {@code tokenTemplate} are ignored — only its name/color/subtypes/etc. are used. If the
 * controller has no matching creature, nothing happens.
 *
 * <p>The single-token, power-sized analog of
 * {@link SacrificeCreatureToCreateTokensEqualToToughnessEffect} (which instead creates X copies).
 * Used by Ooze Garden.
 *
 * @param tokenTemplate the token to create (name/color/subtypes/etc.); its power/toughness/amount are ignored
 * @param filter        predicate restricting which creatures may be sacrificed
 */
public record SacrificeCreatureCreateSizedTokenEqualToPowerEffect(
        CreateTokenEffect tokenTemplate,
        PermanentPredicate filter
) implements CardEffect {
}
