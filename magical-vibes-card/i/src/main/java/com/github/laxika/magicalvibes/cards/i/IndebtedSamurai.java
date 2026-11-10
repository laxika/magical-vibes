package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "9")
public class IndebtedSamurai extends Card {

    public IndebtedSamurai() {
        // Bushido 1
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BushidoEffect(1));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(1));

        // Whenever a Samurai you control dies, you may put a +1/+1 counter on this creature.
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.SAMURAI),
                        ObjectRelation.YOU_CONTROL),
                new MayEffect(new PutCountersOnSourceEffect(1, 1, 1), "Put a +1/+1 counter on Indebted Samurai?"));
    }
}
