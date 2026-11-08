package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-condition record for Goblin Morale Sergeant's "whenever this enlists a nontoken creature" (on
 * {@code Triggers.attackingCreatureYouControlAbilityTriggers()}). Never resolved directly: the collector judges the
 * enlistment and puts {@link #triggeredEffect()} on the stack with the enlisted creature as the entry's triggering
 * permanent.
 */
public record ConjureDuplicateOfEnlistedNontokenCreatureEffect() implements CardEffect {

    /** "You may conjure a duplicate of that creature into the top five cards of your library at random." */
    public CardEffect triggeredEffect() {
        return new MayEffect(new ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect(),
                "Conjure a duplicate of the enlisted creature into the top five cards of your library?");
    }
}
