package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "68")
public class DeadlyVisit extends Card {

    public DeadlyVisit() {
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect());
        addSpellEffect(new SurveilEffect(2));
    }
}
