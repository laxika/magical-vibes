package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "213")
public class DevourerOfMemory extends Card {

    public DevourerOfMemory() {
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, SequenceEffect.of(
                BoostEffect.self(1, 1),
                MakeCreatureUnblockableEffect.self()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{B}",
                List.of(new MillEffect(1, Subjects.you())),
                "{1}{U}{B}: Mill a card."));
    }
}
