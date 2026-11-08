package com.github.laxika.magicalvibes.model.effect;

/**
 * An effect that adds mana as the ability it belongs to resolves, written on an ability that triggers from a mana
 * ability's activation ("whenever a player taps a land for mana, that player adds …"). An ability with one, and no
 * target, is a triggered mana ability (CR 605.1b): it doesn't go on the stack and resolves as soon as it triggers
 * (CR 605.4a), so its land-tap collector resolves it at once.
 */
public interface TriggeredManaAbilityEffect extends CardEffect {
}
