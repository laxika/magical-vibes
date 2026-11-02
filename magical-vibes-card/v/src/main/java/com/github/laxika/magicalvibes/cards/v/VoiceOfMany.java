package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithFewerCreaturesThanController;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C19", collectorNumber = "36")
@CardRegistration(set = "KHC", collectorNumber = "77")
@CardRegistration(set = "SCD", collectorNumber = "215")
public class VoiceOfMany extends Card {

    public VoiceOfMany() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(new OpponentsWithFewerCreaturesThanController()));
    }
}
