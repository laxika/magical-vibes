package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "173")
public class EarthKingdomGeneral extends Card {

    public EarthKingdomGeneral() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EarthbendTargetLandEffect(2));
        addTrigger(Triggers.youPutPlusOneCountersOnCreature(),
                OncePerTurnTriggerEffect.markOnAcceptance(
                        new MayEffect(new GainLifeEffect(new EventValue()), "Gain that much life?")));
    }
}
