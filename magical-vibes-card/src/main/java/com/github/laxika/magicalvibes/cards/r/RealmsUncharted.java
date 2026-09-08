package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GiftsUngivenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ROE", collectorNumber = "206")
public class RealmsUncharted extends Card {

    public RealmsUncharted() {
        addEffect(EffectSlot.SPELL,
                new GiftsUngivenEffect(new CardTypePredicate(CardType.LAND), false));
    }
}
