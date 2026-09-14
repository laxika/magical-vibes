package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Reveals the top planar card and optionally puts it on the bottom of the planar deck. */
public record RevealTopPlanarCardMayPutOnBottomEffect(Card revealedCard) implements CardEffect {

    /** Creates the initial effect, before the revealed card is captured. */
    public RevealTopPlanarCardMayPutOnBottomEffect() {
        this(null);
    }
}
