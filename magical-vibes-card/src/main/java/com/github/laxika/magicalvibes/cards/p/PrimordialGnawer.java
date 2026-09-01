package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;

@CardRegistration(set = "LCI", collectorNumber = "114")
public class PrimordialGnawer extends Card {

    public PrimordialGnawer() {
        addEffect(EffectSlot.ON_DEATH, new DiscoverEffect(3));
    }
}
