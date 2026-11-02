package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "221")
public class VoidwingHybrid extends Card {

    public VoidwingHybrid() {
        addStaticEffect(new ToxicEffect(1));
        addTrigger(new TriggerCondition(Triggers.youProliferate(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
