package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect for Knowledge Pool's ON_ANY_PLAYER_CASTS_SPELL trigger.
 * When a player casts a spell from their hand, exile it and let them
 * cast a nonland card from among other cards exiled with Knowledge Pool
 * without paying its mana cost.
 */
public record KnowledgePoolCastTriggerEffect() implements CardEffect {
}
