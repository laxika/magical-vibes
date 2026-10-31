package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5DN", collectorNumber = "60")
public class ViciousBetrayal extends Card {

    public ViciousBetrayal() {
        addCastingCost(new SacrificeAnyNumberOfPermanentsCost(
                new PermanentIsCreaturePredicate()));
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(
                new Scaled(new XValue(), 2), new Scaled(new XValue(), 2)));
    }
}
