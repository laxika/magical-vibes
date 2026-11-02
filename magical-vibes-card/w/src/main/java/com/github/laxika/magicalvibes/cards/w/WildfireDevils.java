package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RandomPlayerExilesInstantOrSorceryAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "183")
@CardRegistration(set = "C19", collectorNumber = "30")
@CardRegistration(set = "SCD", collectorNumber = "170")
public class WildfireDevils extends Card {

    public WildfireDevils() {
        RandomPlayerExilesInstantOrSorceryAndMayCastCopyEffect ability =
                new RandomPlayerExilesInstantOrSorceryAndMayCastCopyEffect();
        addTrigger(Triggers.enters(ObjectRelation.THIS), ability);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, ability);
    }
}
