package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a targeting restriction to the target permanent until end of turn.
 *
 * <p>This is separate from a keyword grant because some cards create a hexproof-like restriction
 * without granting the hexproof keyword or its special interactions.</p>
 */
public record GrantTargetingRestrictionToTargetUntilEndOfTurnEffect(
        TargetingRestrictionEffect restriction) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
