package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "76")
@CardRegistration(set = "PIP", collectorNumber = "604")
public class GlowingOne extends Card {

    public GlowingOne() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GiveTargetPlayerRadCountersEffect(4));
        addTrigger(new TriggerCondition(Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)),
                TriggerOptions.ONE_OR_MORE, new GainLifeEffect(1));
    }
}
