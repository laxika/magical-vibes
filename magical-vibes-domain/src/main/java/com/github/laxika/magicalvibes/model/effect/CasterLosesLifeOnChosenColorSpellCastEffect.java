package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for "whenever a player casts a spell of this permanent's chosen color,
 * that player loses N life" (Curse of Wizardry).
 */
public record CasterLosesLifeOnChosenColorSpellCastEffect(int amount) implements CardEffect {
}
