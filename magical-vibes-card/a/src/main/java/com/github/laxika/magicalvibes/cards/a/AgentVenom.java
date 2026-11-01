package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "49")
@CardRegistration(set = "SPM", collectorNumber = "255")
@CardRegistration(set = "OM1", collectorNumber = "66")
public class AgentVenom extends Card {

    public AgentVenom() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));
    }
}
