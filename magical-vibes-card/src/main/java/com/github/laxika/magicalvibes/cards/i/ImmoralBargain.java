package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOC", collectorNumber = "47")
@CardRegistration(set = "SOC", collectorNumber = "95")
public class ImmoralBargain extends Card {

    public ImmoralBargain() {
        addEffect(EffectSlot.SPELL, new SacrificeAnyNumberOfPermanentsCost(
                new PermanentIsCreaturePredicate()));

        targetX(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Targets must be nonland permanents"
        ), 100).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
