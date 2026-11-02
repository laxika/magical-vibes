package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "98")
public class FatalAttraction extends Card {

    public FatalAttraction() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DealDamageToEnchantedCreatureEffect(2))
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DealDamageToEnchantedCreatureEffect(4));
    }
}
