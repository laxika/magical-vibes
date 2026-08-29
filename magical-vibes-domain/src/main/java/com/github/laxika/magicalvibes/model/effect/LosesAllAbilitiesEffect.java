package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Causes permanents matching the given scope to lose all abilities.
 * Keywords, activated abilities, triggered abilities, and static abilities of the
 * affected permanent are suppressed.
 * <p>
 * With {@link EffectDuration#CONTINUOUS}: static effect used by auras like Deep Freeze and
 * resolved by the static-effect handler.
 * <p>
 * With a non-continuous duration: one-shot effect that sets a temporary flag or floating
 * layer effect on the permanent. Used by cards like Merfolk Trickster.
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
        return duration != EffectDuration.CONTINUOUS
                ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
