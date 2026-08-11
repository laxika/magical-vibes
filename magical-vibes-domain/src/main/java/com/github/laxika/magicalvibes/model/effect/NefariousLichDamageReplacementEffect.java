package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect for Nefarious Lich: damage to its controller is replaced by exiling
 * that many cards from that player's graveyard, with failure causing that player to lose the game.
 */
public record NefariousLichDamageReplacementEffect() implements CardEffect {
}
