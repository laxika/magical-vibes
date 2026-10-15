package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsReturnSelfIfCardTypeEffect;

@CardRegistration(set = "SOM", collectorNumber = "76")
public class PsychicMiasma extends Card {

    public PsychicMiasma() {
        addEffect(EffectSlot.SPELL, new TargetPlayerDiscardsReturnSelfIfCardTypeEffect(1, CardType.LAND));
    }
}
