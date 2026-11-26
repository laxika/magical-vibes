package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "103")
@CardRegistration(set = "MKM", collectorNumber = "301")
public class SliceFromTheShadows extends Card {

    public SliceFromTheShadows() {
        addStaticEffect(new CantBeCounteredEffect());
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(new Scaled(new XValue(), -1), new Scaled(new XValue(), -1)));
    }
}
