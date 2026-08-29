package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;

@CardRegistration(set = "MMQ", collectorNumber = "65")
public class ChameleonSpirit extends Card {

    public ChameleonSpirit() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        PermanentCount matchingOpponentPermanents = new PermanentCount(
                new PermanentHasSourceChosenColorPredicate(), CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(matchingOpponentPermanents, matchingOpponentPermanents));
    }
}
