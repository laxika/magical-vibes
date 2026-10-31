package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSH", collectorNumber = "99")
public class HourOfDefeat extends Card {

    public HourOfDefeat() {
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect());
        addSpellEffect(new SurveilEffect(1));
    }
}
