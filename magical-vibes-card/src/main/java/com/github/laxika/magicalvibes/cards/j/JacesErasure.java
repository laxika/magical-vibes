package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

@CardRegistration(set = "M11", collectorNumber = "59")
public class JacesErasure extends Card {

    public JacesErasure() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new MayEffect(
                new MillTargetPlayerEffect(1),
                "Have target player mill a card?"
        ));
    }
}
