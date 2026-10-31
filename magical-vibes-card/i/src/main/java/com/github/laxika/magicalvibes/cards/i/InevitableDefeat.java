package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "194")
public class InevitableDefeat extends Card {

    public InevitableDefeat() {
        addStaticEffect(new CantBeCounteredEffect());
        target(TargetFilters.nonlandPermanent())
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER))
                .addSpellEffect(new GainLifeEffect(3));
    }
}
