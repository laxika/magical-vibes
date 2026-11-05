package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.TriggeringCardMatches;

@CardRegistration(set = "SPE", collectorNumber = "12")
public class VenomEddieBrock extends Card {

    public VenomEddieBrock() {
        // One ability: the counter is always put on Venom, and the draw happens only if a Villain died.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), SequenceEffect.of(
                new PutCountersOnSourceEffect(1, 1, 1),
                new ConditionalEffect(new TriggeringCardMatches(
                        new CardSubtypePredicate(CardSubtype.VILLAIN)), new DrawCardEffect())));
    }
}
