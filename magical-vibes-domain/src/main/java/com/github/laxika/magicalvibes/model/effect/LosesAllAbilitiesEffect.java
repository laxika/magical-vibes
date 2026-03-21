package com.github.laxika.magicalvibes.model.effect;

/**
 * Causes permanents matching the given scope to lose all abilities.
 * Keywords, activated abilities, triggered abilities, and static abilities of the
 * affected permanent are suppressed.
 * <p>
 * With {@link EffectDuration#CONTINUOUS}: static effect used by auras like Deep Freeze.
 * Resolved by {@code StaticEffectResolutionService}.
 * <p>
 * With {@link EffectDuration#UNTIL_END_OF_TURN}: one-shot effect that sets a temporary flag
 * on the permanent, cleared by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 * Used by cards like Merfolk Trickster. Resolved by {@code KeywordGrantResolutionService}.
 *
 * @param scope    which permanents are affected (ENCHANTED_CREATURE, TARGET, etc.)
 * @param duration how long the effect lasts
 */
public record LosesAllAbilitiesEffect(GrantScope scope, EffectDuration duration) implements CardEffect {

    /** Convenience constructor defaulting to {@link EffectDuration#CONTINUOUS}. */
    public LosesAllAbilitiesEffect(GrantScope scope) {
        this(scope, EffectDuration.CONTINUOUS);
    }

    @Override
    public boolean canTargetPermanent() {
        return duration == EffectDuration.UNTIL_END_OF_TURN;
    }
}
