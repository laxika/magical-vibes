package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "SOK", collectorNumber = "105")
public class InnerFire extends Card {

    public InnerFire() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.RED, new CardsInHand(CountScope.CONTROLLER)));
    }
}
