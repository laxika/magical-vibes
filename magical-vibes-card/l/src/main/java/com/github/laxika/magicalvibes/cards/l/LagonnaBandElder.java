package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "21")
public class LagonnaBandElder extends Card {

    public LagonnaBandElder() {
        // When this creature enters, if you control an enchantment, you gain 3 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new ControlsPermanent(new PermanentIsEnchantmentPredicate()),
                new GainLifeEffect(3)));
    }
}
