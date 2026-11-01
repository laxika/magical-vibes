package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "59")
public class IllusoryGains extends Card {

    public IllusoryGains() {
        target(TargetFilters.creature())
                .addStaticEffect(new ControlEnchantedCreatureEffect())
                .addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS),
                        new AttachSourceAuraToEnteringCreatureEffect(false));
    }
}
