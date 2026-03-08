package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "95")
public class SlagFiend extends Card {

    public SlagFiend() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToCardsInAllGraveyardsEffect(new CardTypePredicate(CardType.ARTIFACT)));
    }
}
