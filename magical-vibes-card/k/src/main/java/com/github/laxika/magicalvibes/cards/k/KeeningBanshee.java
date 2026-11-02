package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "92")
@CardRegistration(set = "DDD", collectorNumber = "44")
@CardRegistration(set = "GVL", collectorNumber = "44")
@CardRegistration(set = "GK2", collectorNumber = "31")
public class KeeningBanshee extends Card {

    public KeeningBanshee() {
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BoostTargetCreatureEffect(-2, -2));
    }
}
