package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the creature that caused the trigger and links it to the source permanent, so it returns under its owner's
 * control when that source leaves the battlefield (Portcullis). Portcullis's "if there are two or more other creatures
 * on the battlefield" is its ability's intervening "if" ({@code AnotherCreatureOnBattlefield(2)}), not part of this.
 */
public record ExileTriggeringCreatureUntilSourceLeavesEffect() implements CardEffect {
}
