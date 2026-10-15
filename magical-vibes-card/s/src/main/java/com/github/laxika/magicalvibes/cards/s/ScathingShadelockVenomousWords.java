package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VenomousWords;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/** Scathing Shadelock // Venomous Words (SOS 98). */
@CardRegistration(set = "SOS", collectorNumber = "98")
public class ScathingShadelockVenomousWords extends Card {

    public ScathingShadelockVenomousWords() {
        setBackFaceCard(new VenomousWords());

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "VenomousWords";
    }
}
