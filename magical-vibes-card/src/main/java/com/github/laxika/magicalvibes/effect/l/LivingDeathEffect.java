package com.github.laxika.magicalvibes.effect.l;

import com.github.laxika.magicalvibes.model.effect.BoardWipeEffect;

/**
 * Living Death: each player exiles all creature cards from their graveyard, then sacrifices all
 * creatures they control, then puts all cards they exiled this way onto the battlefield. The three
 * steps happen in that order as part of a single resolution, so creatures sacrificed to the second
 * step are never reanimated by the third.
 */
public record LivingDeathEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
