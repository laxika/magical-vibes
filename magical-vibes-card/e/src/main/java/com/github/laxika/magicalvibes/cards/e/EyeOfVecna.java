package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "243")
public class EyeOfVecna extends Card {

    public EyeOfVecna() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(2)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayPayManaEffect("{2}",
                        SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(2)),
                        "Pay {2} to draw a card?"));
    }
}
