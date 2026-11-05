package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "77")
public class SeaGateStormcaller extends Card {

    public SeaGateStormcaller() {
        addStaticEffect(new KickerEffect("{4}{U}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CopyNextInstantOrSorceryCastThisTurnEffect(2),
                new ConditionalEffect(new Kicked(), new CopyNextInstantOrSorceryCastThisTurnEffect(2)));
    }
}
