package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "154")
@CardRegistration(set = "DVD", collectorNumber = "30")
@CardRegistration(set = "5ED", collectorNumber = "174")
@CardRegistration(set = "4ED", collectorNumber = "144")
@CardRegistration(set = "3ED", collectorNumber = "116")
@CardRegistration(set = "SUM", collectorNumber = "116")
@CardRegistration(set = "DDC", collectorNumber = "30")
public class LordOfThePit extends Card {

    public LordOfThePit() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeOtherCreatureOrDamageEffect(7));
    }
}
