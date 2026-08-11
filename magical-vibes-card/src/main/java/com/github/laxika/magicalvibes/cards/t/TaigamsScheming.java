package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "KTK", collectorNumber = "57")
public class TaigamsScheming extends Card {

    public TaigamsScheming() {
        // Surveil 5.
        addEffect(EffectSlot.SPELL, new SurveilEffect(5));
    }
}
