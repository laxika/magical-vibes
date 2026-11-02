package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "11")
@CardRegistration(set = "TMP", collectorNumber = "15")
public class CloudchaserEagle extends Card {

    public CloudchaserEagle() {
        target(TargetFilters.enchantment()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DestroyTargetPermanentEffect());
    }
}
