package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.ItThatRidesAsOne;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "EMN", collectorNumber = "33")
public class LoneRider extends Card {

    public LoneRider() {
        setBackFaceCard(new ItThatRidesAsOne());

        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new ConditionalEffect(new GainedLifeThisTurn(3), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "ItThatRidesAsOne";
    }
}
