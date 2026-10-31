package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "85")
@CardRegistration(set = "2X2", collectorNumber = "88")
public class SeveredStrands extends Card {

    public SeveredStrands() {
        addCastingCost(new SacrificeCreatureCost(false, false, true));
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new GainLifeEffect(new XValue()))
                .addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
