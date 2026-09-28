package com.github.laxika.magicalvibes.model.effect;

/**
 * Global watcher for Ghyrson Starn, Kelermorph: whenever another source controlled by the
 * watcher deals exactly 1 damage to a permanent or player, the watcher deals 2 damage to that
 * recipient. The damage trigger collector expands this marker into a non-targeting any-target
 * damage ability for each qualifying recipient.
 */
public record GhyrsonStarnKelermorphEffect() implements CardEffect {
}
