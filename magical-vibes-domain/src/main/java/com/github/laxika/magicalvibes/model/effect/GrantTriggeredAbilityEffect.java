package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Continuously grants a triggered ability to the permanents selected by {@code scope} (and,
 * for the multi-permanent scopes, {@code filter}). Belongs on {@link EffectSlot#STATIC}.
 *
 * <p>{@code slot} is the trigger condition of the granted ability and {@code grantedEffect}
 * is what it does on resolution — e.g. {@code (ON_DAMAGE_TO_PLAYER, new DrawCardEffect(1),
 * GrantScope.SELF_AND_PAIRED)} for Tandem Lookout's "each of those creatures has 'Whenever
 * this creature deals damage to an opponent, draw a card.'"
 *
 * <p>Unlike {@code GrantEffectToTargetUntilEndOfTurnEffect} the grant is not a one-shot
 * snapshot: it is recomputed by the layer system, so it turns on and off with its condition
 * (e.g. wrapped in {@code ConditionalEffect(new SourceIsPaired(), …)}).
 */
public record GrantTriggeredAbilityEffect(EffectSlot slot, CardEffect grantedEffect, GrantScope scope,
                                          PermanentPredicate filter) implements CardEffect {

    public GrantTriggeredAbilityEffect(EffectSlot slot, CardEffect grantedEffect, GrantScope scope) {
        this(slot, grantedEffect, scope, null);
    }
}
