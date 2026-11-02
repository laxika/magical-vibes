package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "502")
public class HulkAlwaysAngry extends Card {

    public HulkAlwaysAngry() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DestroyAllPermanentsEffect(new PermanentIsArtifactPredicate()));
        addStaticEffect(new MustAttackEffect());
    }
}
