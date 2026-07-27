package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NPH", collectorNumber = "60")
@CardRegistration(set = "5ED", collectorNumber = "160")
@CardRegistration(set = "4ED", collectorNumber = "136")
public class EvilPresence extends Card {

    public EvilPresence() {
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesTypeEffect(CardSubtype.SWAMP));
    }
}
