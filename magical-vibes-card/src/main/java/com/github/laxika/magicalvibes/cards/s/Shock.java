package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "M14", collectorNumber = "155")
@CardRegistration(set = "ONS", collectorNumber = "227")
@CardRegistration(set = "SPM", collectorNumber = "88")
@CardRegistration(set = "BTD", collectorNumber = "45")
@CardRegistration(set = "AER", collectorNumber = "98")
@CardRegistration(set = "M21", collectorNumber = "159")
@CardRegistration(set = "M20", collectorNumber = "160")
@CardRegistration(set = "M19", collectorNumber = "156")
@CardRegistration(set = "M12", collectorNumber = "154")
@CardRegistration(set = "STH", collectorNumber = "98")
@CardRegistration(set = "10E", collectorNumber = "232")
@CardRegistration(set = "9ED", collectorNumber = "220")
@CardRegistration(set = "8ED", collectorNumber = "222")
@CardRegistration(set = "7ED", collectorNumber = "219")
@CardRegistration(set = "6ED", collectorNumber = "206")
public class Shock extends Card {

    public Shock() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
    }
}
