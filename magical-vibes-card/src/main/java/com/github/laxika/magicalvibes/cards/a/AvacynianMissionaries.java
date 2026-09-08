package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LunarchInquisitors;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "SOI", collectorNumber = "6")
public class AvacynianMissionaries extends Card {

    public AvacynianMissionaries() {
        setBackFaceCard(new LunarchInquisitors());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new Equipped(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "LunarchInquisitors";
    }
}
