package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOM", collectorNumber = "103")
@CardRegistration(set = "MRD", collectorNumber = "105")
@CardRegistration(set = "KTK", collectorNumber = "120")
@CardRegistration(set = "M10", collectorNumber = "155")
@CardRegistration(set = "9ED", collectorNumber = "218")
@CardRegistration(set = "8ED", collectorNumber = "220")
@CardRegistration(set = "7ED", collectorNumber = "217")
@CardRegistration(set = "6ED", collectorNumber = "204")
@CardRegistration(set = "5ED", collectorNumber = "265")
@CardRegistration(set = "4ED", collectorNumber = "219")
@CardRegistration(set = "ICE", collectorNumber = "216")
@CardRegistration(set = "TMP", collectorNumber = "203")
public class Shatter extends Card {

    public Shatter() {
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
