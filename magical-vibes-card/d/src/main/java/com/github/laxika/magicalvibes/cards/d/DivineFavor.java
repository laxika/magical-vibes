package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "14")
@CardRegistration(set = "M13", collectorNumber = "11")
@CardRegistration(set = "M14", collectorNumber = "17")
@CardRegistration(set = "M15", collectorNumber = "10")
@CardRegistration(set = "MB1", collectorNumber = "80")
public class DivineFavor extends Card {

    public DivineFavor() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3))
                .addStaticEffect(new StaticBoostEffect(1, 3, GrantScope.ENCHANTED_CREATURE));
    }
}
