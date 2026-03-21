package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect that causes the target permanent to lose all abilities until end of turn.
 * Keywords, activated abilities, triggered abilities, and static abilities of the
 * affected permanent are suppressed until the cleanup step.
 * <p>
 * Used by cards like Merfolk Trickster ("It loses all abilities until end of turn.").
 * Unlike {@link LosesAllAbilitiesEffect}, which is a static/continuous effect from auras,
 * this is a one-shot effect that sets a temporary flag on the permanent, cleared by
 * {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 */
public record LoseAllAbilitiesUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
