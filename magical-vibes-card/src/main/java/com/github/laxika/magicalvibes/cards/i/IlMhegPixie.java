package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "FIN", collectorNumber = "57")
public class IlMhegPixie extends Card {

    public IlMhegPixie() {
        addEffect(EffectSlot.ON_ATTACK, new SurveilEffect(1));
    }
}
