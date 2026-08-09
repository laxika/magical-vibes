package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureThenMassDamageEqualToPowerEffect;

@CardRegistration(set = "NEM", collectorNumber = "97")
public class Rupture extends Card {

    public Rupture() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureThenMassDamageEqualToPowerEffect());
    }
}
