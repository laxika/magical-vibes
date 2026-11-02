package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "86")
@CardRegistration(set = "MKM", collectorNumber = "343")
public class HomicideInvestigator extends Card {

    public HomicideInvestigator() {
        // "Whenever one or more nontoken creatures you control die" includes this creature itself;
        // the once-per-turn wrapper keeps simultaneous deaths to a single investigate.
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                CreateTokenEffect.ofClueToken(1));
    }
}
