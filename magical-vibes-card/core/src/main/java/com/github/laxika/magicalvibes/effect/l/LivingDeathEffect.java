package com.github.laxika.magicalvibes.effect.l;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoardWipeEffect;

/**
 * Living Death-style effect: each player exiles all cards of the selected type from their
 * graveyard, then sacrifices all permanents of that type they control, then puts all cards they
 * exiled this way onto the battlefield. The three steps happen in that order as part of a single
 * resolution, so permanents sacrificed to the second step are never reanimated by the third.
 */
public record LivingDeathEffect(CardType cardType) implements BoardWipeEffect {

    public LivingDeathEffect() {
        this(CardType.CREATURE);
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
