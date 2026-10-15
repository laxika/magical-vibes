package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenConjuresFromAdjacentPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerDirection;

@CardRegistration(set = "YMKM", collectorNumber = "25")
public class JuggleThePerformance extends Card {

    public JuggleThePerformance() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerDiscardsHandThenConjuresFromAdjacentPlayerLibraryEffect(
                        7, PlayerDirection.RIGHT));
    }
}
