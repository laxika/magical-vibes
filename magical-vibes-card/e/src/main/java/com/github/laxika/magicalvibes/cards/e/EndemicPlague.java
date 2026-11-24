package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithPermanentsPredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "142")
public class EndemicPlague extends Card {

    public EndemicPlague() {
        addCastingCost(SacrificeCreatureCost.withPermanentSnapshot());
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentSharesCreatureTypeWithPermanentsPredicate(Subjects.paidAsCost()))), true));
    }
}
