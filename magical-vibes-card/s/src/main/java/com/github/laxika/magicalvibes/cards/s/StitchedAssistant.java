package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "81")
@CardRegistration(set = "DBL", collectorNumber = "348")
public class StitchedAssistant extends Card {

    public StitchedAssistant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));

        addTrigger(Triggers.exploits(), new ScryEffect(1),
                new DrawCardEffect(1));
    }
}
