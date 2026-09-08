package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KHM", collectorNumber = "53")
public class DepartTheRealm extends Card {

    public DepartTheRealm() {
        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addCastingOption(new ForetellCast("{U}"));
    }
}
