package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement for Lich's Duel Mastery: replace its controller's life loss with putting a
 * face-down shield exiled with the source into that player's hand, or sacrificing the source when
 * no shield remains.
 */
public record LichDuelMasteryLifeLossReplacementEffect() implements CardEffect {
}
