package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;

@CardRegistration(set = "ISD", collectorNumber = "27")
public class PurifyTheGrave extends Card {

    public PurifyTheGrave() {
        addEffect(EffectSlot.SPELL, new ExileTargetCardFromGraveyardEffect(null));
        addCastingOption(new FlashbackCast("{W}"));
    }
}
