package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeCardFromOutsideGameWithHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "MB1", collectorNumber = "14")
public class WizenedArbiter extends Card {

    public WizenedArbiter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExchangeCardFromOutsideGameWithHandEffect(new CardColorPredicate(CardColor.WHITE)));
    }
}
