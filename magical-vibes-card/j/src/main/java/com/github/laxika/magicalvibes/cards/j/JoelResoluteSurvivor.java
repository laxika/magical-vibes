package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2198")
public class JoelResoluteSurvivor extends Card {

    public JoelResoluteSurvivor() {
        // Whenever a creature token dies, put a +1/+1 counter on Joel and draw a card.
        // This ability triggers only once each turn.
        addTrigger(Triggers.creatureDies(new PermanentIsTokenPredicate(), ObjectRelation.ANY),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                SequenceEffect.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE), new DrawCardEffect(1)));
    }
}
