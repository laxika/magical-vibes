package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "205")
@CardRegistration(set = "MKM", collectorNumber = "361")
@CardRegistration(set = "MKM", collectorNumber = "424")
public class GleamingGeardrake extends Card {

    public GleamingGeardrake() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofClueToken(1));
        addTrigger(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsArtifactPredicate(),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ));
    }
}
