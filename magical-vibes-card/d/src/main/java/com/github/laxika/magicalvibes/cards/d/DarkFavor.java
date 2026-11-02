package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "89")
@CardRegistration(set = "M13", collectorNumber = "86")
@CardRegistration(set = "M14", collectorNumber = "92")
public class DarkFavor extends Card {

    public DarkFavor() {
        // Enchant creature; on entry the Aura's controller loses 1 life, and the enchanted creature gets +3/+1.
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(1))
                .addStaticEffect(new StaticBoostEffect(3, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
