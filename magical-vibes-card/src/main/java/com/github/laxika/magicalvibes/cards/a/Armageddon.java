package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "5")
@CardRegistration(set = "P02", collectorNumber = "12")
public class Armageddon extends Card {

    public Armageddon() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsLandPredicate()));
    }
}
