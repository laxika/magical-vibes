package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "126")
public class HellToPay extends Card {

    public HellToPay() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(new XValue()))
                .addSpellEffect(CreateTokenEffect.ofTappedTreasureToken(new EventValue()));
    }
}
