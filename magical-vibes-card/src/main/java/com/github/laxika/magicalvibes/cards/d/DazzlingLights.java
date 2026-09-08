package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "GRN", collectorNumber = "34")
public class DazzlingLights extends Card {

    public DazzlingLights() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-3, 0));
        addEffect(EffectSlot.SPELL, new SurveilEffect(2));
    }
}
