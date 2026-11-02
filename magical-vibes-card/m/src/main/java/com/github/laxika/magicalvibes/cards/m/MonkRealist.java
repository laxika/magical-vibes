package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "21")
@CardRegistration(set = "CMD", collectorNumber = "20")
public class MonkRealist extends Card {

    public MonkRealist() {
        target(TargetFilters.enchantment()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DestroyTargetPermanentEffect());
    }
}
