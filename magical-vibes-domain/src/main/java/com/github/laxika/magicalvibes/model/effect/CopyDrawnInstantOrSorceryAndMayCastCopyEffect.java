package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Copies the instant or sorcery revealed by God-Eternal Kefnet and offers the copy for casting. */
public record CopyDrawnInstantOrSorceryAndMayCastCopyEffect(Card drawnCard) implements CardEffect {
}
