package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastPermanentSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "199")
@CardRegistration(set = "FDN", collectorNumber = "243")
@CardRegistration(set = "FDN", collectorNumber = "430")
@CardRegistration(set = "FDN", collectorNumber = "440")
public class MuldrothaTheGravetide extends Card {

    public MuldrothaTheGravetide() {
        addEffect(EffectSlot.STATIC, new PlayLandsFromGraveyardEffect());
        addEffect(EffectSlot.STATIC, new CastPermanentSpellsFromGraveyardEffect());
    }
}
