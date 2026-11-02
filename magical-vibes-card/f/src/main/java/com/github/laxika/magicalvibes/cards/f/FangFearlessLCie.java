package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "99")
@CardRegistration(set = "FIN", collectorNumber = "381")
@CardRegistration(set = "FIN", collectorNumber = "446")
@CardRegistration(set = "FIN", collectorNumber = "526")
public class FangFearlessLCie extends Card {

    public FangFearlessLCie() {
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
