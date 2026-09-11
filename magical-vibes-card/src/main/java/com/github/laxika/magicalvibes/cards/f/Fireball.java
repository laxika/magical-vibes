package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "136")
@CardRegistration(set = "DD2", collectorNumber = "56")
@CardRegistration(set = "M12", collectorNumber = "131")
@CardRegistration(set = "M11", collectorNumber = "138")
@CardRegistration(set = "ITP", collectorNumber = "32")
@CardRegistration(set = "5ED", collectorNumber = "227")
@CardRegistration(set = "4ED", collectorNumber = "192")
@CardRegistration(set = "DST", collectorNumber = "60")
@CardRegistration(set = "RQS", collectorNumber = "31")
@CardRegistration(set = "ATH", collectorNumber = "29")
@CardRegistration(set = "BTD", collectorNumber = "37")
@CardRegistration(set = "SUM", collectorNumber = "150")
@CardRegistration(set = "3ED", collectorNumber = "150")
@CardRegistration(set = "PD2", collectorNumber = "27")
@CardRegistration(set = "JVC", collectorNumber = "56")
public class Fireball extends Card {

    public Fireball() {
        setAdditionalCostPerExtraTarget(1);
        target(0, 99).addEffect(EffectSlot.SPELL, DealDividedDamageEffect.xDividedEvenly());
    }
}
