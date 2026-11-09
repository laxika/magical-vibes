package com.github.laxika.magicalvibes.model.effect;

/**
 * "Copy that card and you may cast the copy. That copy costs {2} less to cast." (God-Eternal Kefnet): copies the stack
 * entry's triggering card, the instant or sorcery card revealed as it was drawn, as it was then (its last-known
 * information if it left the hand meanwhile), and offers the copy for casting as the ability resolves.
 */
public record CopyDrawnInstantOrSorceryAndMayCastCopyEffect() implements CardEffect {
}
