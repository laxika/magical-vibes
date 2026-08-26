package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreaturesThenReturnCreatureCardsEffect;

@CardRegistration(set = "LCI", collectorNumber = "94")
@CardRegistration(set = "LCI", collectorNumber = "337")
public class BringerOfTheLastGift extends Card {

    public BringerOfTheLastGift() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new WasCast(),
                new SacrificeOtherCreaturesThenReturnCreatureCardsEffect()));
    }
}
