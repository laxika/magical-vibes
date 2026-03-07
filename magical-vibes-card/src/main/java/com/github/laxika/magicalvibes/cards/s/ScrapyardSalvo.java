package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect;

@CardRegistration(set = "NPH", collectorNumber = "94")
public class ScrapyardSalvo extends Card {

    public ScrapyardSalvo() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect(CardType.ARTIFACT));
    }
}
