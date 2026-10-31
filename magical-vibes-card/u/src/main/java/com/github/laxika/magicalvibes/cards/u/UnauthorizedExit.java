package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "74")
@CardRegistration(set = "MKM", collectorNumber = "298")
public class UnauthorizedExit extends Card {

    public UnauthorizedExit() {
        target(TargetFilters.nonlandPermanent()).addSpellEffect(ReturnToHandEffect.target());
        addSpellEffect(new SurveilEffect(1));
    }
}
