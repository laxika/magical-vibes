package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "TLA", collectorNumber = "173")
public class EarthKingdomGeneral extends Card {

    public EarthKingdomGeneral() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EarthbendTargetLandEffect(2));
        addEffect(EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE,
                OncePerTurnTriggerEffect.markOnAcceptance(
                        new MayEffect(new GainLifeEffect(new EventValue()), "Gain that much life?")));
    }
}
