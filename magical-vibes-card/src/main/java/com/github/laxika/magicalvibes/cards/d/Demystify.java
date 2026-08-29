package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "8ED", collectorNumber = "16")
@CardRegistration(set = "9ED", collectorNumber = "13")
@CardRegistration(set = "10E", collectorNumber = "14")
@CardRegistration(set = "M12", collectorNumber = "13")
@CardRegistration(set = "XLN", collectorNumber = "8")
@CardRegistration(set = "ROE", collectorNumber = "18")
public class Demystify extends Card {

    public Demystify() {
        target(TargetFilters.enchantment()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
