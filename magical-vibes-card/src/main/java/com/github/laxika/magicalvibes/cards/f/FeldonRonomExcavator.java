package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextTurnEffect;

@CardRegistration(set = "BRO", collectorNumber = "135")
public class FeldonRonomExcavator extends Card {

    public FeldonRonomExcavator() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(new EventValue()));
    }
}
