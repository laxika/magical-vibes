package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CacklingCulprit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "VOW", collectorNumber = "28")
public class PanickedBystander extends Card {

    public PanickedBystander() {
        setBackFaceCard(new CacklingCulprit());

        var deathTrigger = new GainLifeEffect(1);
        addEffect(EffectSlot.ON_DEATH, deathTrigger);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, deathTrigger);
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new GainedLifeThisTurn(3), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "CacklingCulprit";
    }
}
