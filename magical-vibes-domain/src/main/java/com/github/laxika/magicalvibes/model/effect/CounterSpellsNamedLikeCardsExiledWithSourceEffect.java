package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters every spell on the stack whose name matches a card exiled with the source permanent
 * (Grimoire Thief's "{U}, Sacrifice this creature: Turn all cards exiled with this creature face
 * up. Counter all spells with those names."). Does not target; resolves against the whole stack.
 * The exiled cards are looked up via the resolving entry's {@code sourcePermanentId}, which survives
 * the sacrifice cost, and remain in exile afterwards.
 */
public record CounterSpellsNamedLikeCardsExiledWithSourceEffect() implements CardEffect {
}
