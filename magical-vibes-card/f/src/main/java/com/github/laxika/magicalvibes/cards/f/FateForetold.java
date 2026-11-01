package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "48")
public class FateForetold extends Card {

    public FateForetold() {
        // Enchant creature
        target(TargetFilters.creature())
                // When this Aura enters, draw a card.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect())
                // When enchanted creature dies, its controller draws a card.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new DrawCardForTargetPlayerEffect(1));
    }
}
