package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.EnteredFromZone;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "163")
public class PhyrexianDragonEngine extends Card {

    public PhyrexianDragonEngine() {
        addTrigger(Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.THIS), TriggerOptions.DEFAULT.withEventCondition(
                new EnteredFromZone(Zone.GRAVEYARD, true)),
                new MayEffect(
                        SequenceEffect.of(new DiscardHandEffect(), new DrawCardEffect(3)),
                        "Discard your hand?"));

        addUnearth("{3}{R}{R}");
    }
}
