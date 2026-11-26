package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "58")
@CardRegistration(set = "PLC", collectorNumber = "70")
@CardRegistration(set = "DDD", collectorNumber = "58")
@CardRegistration(set = "GVL", collectorNumber = "58")
@CardRegistration(set = "TSR", collectorNumber = "113")
public class Enslave extends Card {

    public Enslave() {
        target(TargetFilters.creature())
                .addStaticEffect(new ControlEnchantedCreatureEffect())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        DealDamageEffect.to(Subjects.ownerOf(Subjects.attachedCreature()), 1)
                                .dealtBy(Subjects.attachedCreature()));
    }
}
