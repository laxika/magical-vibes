package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Causes permanents matching the given scope to lose all abilities.
 * Keywords, activated abilities, triggered abilities, and static abilities of the
 * affected permanent are suppressed.
 * <p>
 * With {@link EffectDuration#CONTINUOUS}: static effect used by auras like Deep Freeze.
 * Resolved by {@code LosesAllAbilitiesEffectHandler} in {@code staticfx}.
 * <p>
 * With {@link EffectDuration#UNTIL_END_OF_TURN}: one-shot effect that sets a temporary flag
 * on the permanent, cleared by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 * Used by cards like Merfolk Trickster. Resolved by {@code KeywordGrantResolutionService}.
 * Source-linked durations create a floating layer effect that lasts while the source remains on
 * the battlefield.
 *
 * @param scope    which permanents are affected (ENCHANTED_CREATURE, TARGET, etc.)
 * @param filter   optional additional permanent filter
 * @param duration how long the effect lasts
 */
public record LosesAllAbilitiesEffect(GrantScope scope, PermanentPredicate filter,
                                      EffectDuration duration) implements CardEffect {

    /** Convenience constructor defaulting to {@link EffectDuration#CONTINUOUS}. */
    public LosesAllAbilitiesEffect(GrantScope scope) {
        this(scope, null, EffectDuration.CONTINUOUS);
    }

    public LosesAllAbilitiesEffect(GrantScope scope, EffectDuration duration) {
        this(scope, null, duration);
    }

    public LosesAllAbilitiesEffect(GrantScope scope, PermanentPredicate filter) {
        this(scope, filter, EffectDuration.CONTINUOUS);
    }

    @Override
    public TargetSpec targetSpec() {
        if (scope == GrantScope.TARGET_PLAYERS_CREATURES) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (scope == GrantScope.OWN_CREATURES) {
            return TargetSpec.NONE;
        }
        if (scope == GrantScope.SELF) {
            return new TargetSpec(null, false, null, true, 1);
        }
        return duration == EffectDuration.UNTIL_END_OF_TURN
                || duration == EffectDuration.PERMANENT
                || duration == EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD
                || duration == EffectDuration.WHILE_SOURCE_REMAINS
                ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
