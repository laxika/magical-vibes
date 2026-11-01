package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachAuraAttachedToDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "101")
@CardRegistration(set = "SOC", collectorNumber = "215")
public class HatefulEidolon extends Card {

    public HatefulEidolon() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new DrawCardForEachAuraAttachedToDyingCreatureEffect());
    }
}
