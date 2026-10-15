package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "EOC", collectorNumber = "19")
@CardRegistration(set = "EOC", collectorNumber = "39")
public class SurgeConductor extends Card {

    public SurgeConductor() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD,
                new ProliferateEffect());
    }
}
