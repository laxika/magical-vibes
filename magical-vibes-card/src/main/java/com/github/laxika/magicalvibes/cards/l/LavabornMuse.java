package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;

public class LavabornMuse extends Card {

    public LavabornMuse() {
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new DealDamageIfFewCardsInHandEffect(2, 3));
    }
}
