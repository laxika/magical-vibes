package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MB2", collectorNumber = "288")
@CardRegistration(set = "MB2", collectorNumber = "524")
public class TaxTaker extends Card {

    public TaxTaker() {
        addEffect(EffectSlot.ON_OPPONENT_PAYS_TAX,
                CreateTokenEffect.ofTreasureToken(new EventValue()));
    }
}
