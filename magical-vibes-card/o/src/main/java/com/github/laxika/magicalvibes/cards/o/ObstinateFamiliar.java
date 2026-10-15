package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ObstinateFamiliarDrawReplacementEffect;

@CardRegistration(set = "ODY", collectorNumber = "210")
public class ObstinateFamiliar extends Card {

    public ObstinateFamiliar() {
        addEffect(EffectSlot.STATIC, new ObstinateFamiliarDrawReplacementEffect());
    }
}
