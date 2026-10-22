package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Reveals the card just drawn "as you draw it" and, when it is an instant or sorcery card, puts a
 * triggered ability with {@code triggeredEffect} onto the stack ("Whenever you reveal an instant or
 * sorcery card this way, ..."). Pauses the draw instruction so a multi-card draw continues only
 * after the reveal decision (Inquisitor Eisenhorn, God-Eternal Kefnet).
 */
public record RevealDrawnCardIfInstantOrSorceryEffect(Card drawnCard, CardEffect triggeredEffect)
        implements CardEffect {

    @Override
    public boolean pausesDrawInstruction() {
        return true;
    }
}
