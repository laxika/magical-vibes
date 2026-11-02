package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DuelistsConvocationInternationalTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.InitializeDuelistsConvocationInternationalEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
@CardRegistration(set = "MB2", collectorNumber = "293")
@CardRegistration(set = "MB2", collectorNumber = "529")
public class DuelistsConvocationInternational extends Card {

    public DuelistsConvocationInternational() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new InitializeDuelistsConvocationInternationalEffect());
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new DuelistsConvocationInternationalTriggerEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new DuelistsConvocationInternationalTriggerEffect());
    }
}
