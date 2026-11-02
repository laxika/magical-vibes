package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "38")
@CardRegistration(set = "M13", collectorNumber = "39")
@CardRegistration(set = "EMA", collectorNumber = "35")
public class WarPriestOfThune extends Card {

    public WarPriestOfThune() {
        target(TargetFilters.enchantment()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target enchantment?"));
    }
}
