package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageFromOpponentSourcesToControllerAndPlaneswalkersEffect;

@CardRegistration(set = "OMB", collectorNumber = "1")
public class Comeuppance extends Card {

    public Comeuppance() {
        addEffect(EffectSlot.SPELL,
                new PreventAllDamageFromOpponentSourcesToControllerAndPlaneswalkersEffect());
    }
}
