package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Reveals the card just drawn "as you draw it" and, when it is an instant or sorcery card, puts a
 * triggered ability creating {@code tokenEffect}'s token onto the stack ("Whenever you reveal an
 * instant or sorcery card this way, create ..."). Pauses the draw instruction so a multi-card draw
 * continues only after the reveal decision (Inquisitor Eisenhorn).
 */
public record RevealDrawnCardCreateTokenIfInstantOrSorceryEffect(Card drawnCard, CreateTokenEffect tokenEffect)
        implements CardEffect {

    @Override
    public boolean pausesDrawInstruction() {
        return true;
    }
}
