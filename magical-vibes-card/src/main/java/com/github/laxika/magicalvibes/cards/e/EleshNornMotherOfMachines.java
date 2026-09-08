package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentPermanentsEnteringDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "ONE", collectorNumber = "10")
public class EleshNornMotherOfMachines extends Card {

    public EleshNornMotherOfMachines() {
        addEffect(EffectSlot.STATIC, new ETBDoubleTriggerEffect(new CardTruePredicate(), false));
        addEffect(EffectSlot.STATIC, new OpponentPermanentsEnteringDontCauseTriggersEffect());
    }
}
