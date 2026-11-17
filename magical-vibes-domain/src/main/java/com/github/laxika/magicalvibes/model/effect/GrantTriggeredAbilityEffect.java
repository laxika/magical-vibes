package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Continuously grants a triggered ability to the permanents selected by {@code scope} (and,
 * for the multi-permanent scopes, {@code filter}). Added as a static ability.
 *
 * <p>{@code grantedAbility} is the granted ability — e.g. {@code (new TriggeredAbility(
 * Triggers.dealsDamageToOpponent(), new DrawCardEffect(1)), GrantScope.SELF_AND_PAIRED)} for Tandem
 * Lookout's "each of those creatures has 'Whenever this creature deals damage to an opponent, draw
 * a card.'"
 *
 * <p>Unlike {@code GrantEffectToTargetUntilEndOfTurnEffect} the grant is not a one-shot
 * snapshot: it is recomputed by the layer system, so it turns on and off with its condition
 * (e.g. wrapped in {@code ConditionalEffect(new SourceIsPaired(), …)}).
 */
public record GrantTriggeredAbilityEffect(Ability grantedAbility,
                                          GrantScope scope,
                                          PermanentPredicate filter) implements CardEffect {

    /** The grant of {@code grantedAbility}. */
    public GrantTriggeredAbilityEffect(Ability grantedAbility, GrantScope scope) {
        this(grantedAbility, scope, null);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
