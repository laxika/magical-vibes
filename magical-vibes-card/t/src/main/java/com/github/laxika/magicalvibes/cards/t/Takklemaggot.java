package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHR", collectorNumber = "37")
@CardRegistration(set = "LEG", collectorNumber = "119")
@CardRegistration(set = "ME3", collectorNumber = "76")
public class Takklemaggot extends Card {

    public Takklemaggot() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                        new PutCounterOnReferencedPermanentEffect(CounterType.MINUS_ZERO_MINUS_ONE))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect());
    }
}
