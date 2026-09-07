package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "6")
public class BuyYourSilence extends Card {

    public BuyYourSilence() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetPermanentEffect(CreateTokenEffect.ofTreasureToken(1)));
    }
}
