package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "38")
public class UnsettlingTwins extends Card {

    public UnsettlingTwins() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), ManifestDreadEffect.forController());
    }
}
