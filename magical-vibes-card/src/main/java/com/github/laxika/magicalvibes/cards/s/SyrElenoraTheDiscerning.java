package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "ELD", collectorNumber = "67")
public class SyrElenoraTheDiscerning extends Card {

    public SyrElenoraTheDiscerning() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new CardsInHand(CountScope.CONTROLLER), new Fixed(4)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCostForTargetingControlledPermanentEffect(
                new PermanentIsSourcePermanentPredicate(), 2, false));
    }
}
