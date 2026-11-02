package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "46")
@CardRegistration(set = "PIP", collectorNumber = "574")
public class InfestingRadroach extends Card {

    public InfestingRadroach() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new GiveTargetPlayerRadCountersEffect(new EventValue()));
        addTrigger(new TriggerCondition(Triggers.milled(Triggers.NONLAND, ObjectRelation.OPPONENT_OWNS),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayEffect(
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Infesting Radroach from your graveyard to your hand?"));
    }
}
