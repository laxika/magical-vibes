package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "87")
public class Devastate extends Card {

    public Devastate() {
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect());
        addSpellEffect(new MassDamageEffect(1, true));
    }
}
