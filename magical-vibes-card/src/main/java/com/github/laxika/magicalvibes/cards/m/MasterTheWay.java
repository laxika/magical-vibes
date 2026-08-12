package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "KTK", collectorNumber = "188")
public class MasterTheWay extends Card {

    public MasterTheWay() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.SPELL,
                new DealDamageToAnyTargetEffect(new CardsInHand(CountScope.CONTROLLER)));
    }
}
