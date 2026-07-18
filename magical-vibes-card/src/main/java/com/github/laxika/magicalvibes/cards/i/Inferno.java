package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "5ED", collectorNumber = "243")
@CardRegistration(set = "4ED", collectorNumber = "205")
@CardRegistration(set = "8ED", collectorNumber = "196")
@CardRegistration(set = "7ED", collectorNumber = "198")
@CardRegistration(set = "6ED", collectorNumber = "191")
public class Inferno extends Card {

    public Inferno() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(6, true));
    }
}
