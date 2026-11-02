package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "124")
@CardRegistration(set = "DDQ", collectorNumber = "41")
@CardRegistration(set = "SIR", collectorNumber = "124")
public class MindwrackDemon extends Card {

    public MindwrackDemon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(4, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                ConditionalEffect.unless(new NotCondition(new Delirium()), new LoseLifeEffect(4)));
    }
}
