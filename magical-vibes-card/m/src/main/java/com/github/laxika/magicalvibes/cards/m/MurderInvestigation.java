package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "21")
@CardRegistration(set = "ORI", collectorNumber = "27")
public class MurderInvestigation extends Card {

    public MurderInvestigation() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        CreateTokenEffect.whiteSoldier(new EventValue()));
    }
}
