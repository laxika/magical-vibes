package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a temporary targeting restriction for creatures controlled by the effect's controller.
 */
public record GrantTargetingRestrictionToOwnCreaturesUntilEndOfTurnEffect(
        TargetingRestrictionEffect restriction) implements CardEffect {
}
