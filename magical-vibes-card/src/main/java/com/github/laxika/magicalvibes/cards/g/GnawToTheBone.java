package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCreatureCardInGraveyardEffect;

@CardRegistration(set = "ISD", collectorNumber = "183")
public class GnawToTheBone extends Card {

    public GnawToTheBone() {
        addEffect(EffectSlot.SPELL, new GainLifePerCreatureCardInGraveyardEffect(2));
        addCastingOption(new FlashbackCast("{2}{G}"));
    }
}
