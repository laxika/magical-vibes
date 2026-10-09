package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "FRF", collectorNumber = "151")
@CardRegistration(set = "CP3", collectorNumber = "4")
@CardRegistration(set = "C17", collectorNumber = "170")
public class DromokaTheEternal extends Card {

    public DromokaTheEternal() {
        // Whenever a Dragon you control attacks, bolster 2.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                        new BolsterEffect(2)));
    }
}
