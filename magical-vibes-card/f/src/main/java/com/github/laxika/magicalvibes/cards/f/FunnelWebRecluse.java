package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "161")
public class FunnelWebRecluse extends Card {

    public FunnelWebRecluse() {
        // Morbid — When Funnel-Web Recluse enters the battlefield, if a creature died this turn,
        // investigate.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new Morbid(), CreateTokenEffect.ofClueToken(1)));
    }
}
