package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "5")
@CardRegistration(set = "P02", collectorNumber = "12")
@CardRegistration(set = "6ED", collectorNumber = "4")
@CardRegistration(set = "5ED", collectorNumber = "7")
@CardRegistration(set = "4ED", collectorNumber = "5")
@CardRegistration(set = "ATH", collectorNumber = "1")
@CardRegistration(set = "S99", collectorNumber = "6")
@CardRegistration(set = "SUM", collectorNumber = "2")
@CardRegistration(set = "ME1", collectorNumber = "4")
@CardRegistration(set = "3ED", collectorNumber = "2")
@CardRegistration(set = "V14", collectorNumber = "1")
public class Armageddon extends Card {

    public Armageddon() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsLandPredicate()));
    }
}
