package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "106")
public class Bequeathal extends Card {

    public Bequeathal() {
        // Enchant creature
        target(TargetFilters.creature());

        // When enchanted creature dies, you draw two cards.
        addTrigger(Triggers.dies(ObjectRelation.ENCHANTED), new DrawCardEffect(2));
    }
}
