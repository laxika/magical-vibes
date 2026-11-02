package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "105")
public class Scourgemark extends Card {

    public Scourgemark() {
        // Enchant creature
        target(TargetFilters.creature())
                // When this Aura enters, draw a card.
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1))
                // Enchanted creature gets +1/+0.
                .addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.ENCHANTED_CREATURE));
    }
}
