package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "164")
public class NefariousImp extends Card {

    public NefariousImp() {
        addTrigger(new TriggerCondition(Triggers.leavesBattlefield(null, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, new ScryEffect(1));
    }
}
