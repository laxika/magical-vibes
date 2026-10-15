package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "SOK", collectorNumber = "136")
public class MasumaroFirstToLive extends Card {

    public MasumaroFirstToLive() {
        Scaled twiceHandSize = new Scaled(new CardsInHand(CountScope.CONTROLLER), 2);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(twiceHandSize, twiceHandSize));
    }
}
