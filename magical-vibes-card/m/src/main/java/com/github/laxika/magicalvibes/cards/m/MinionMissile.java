package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "663")
public class MinionMissile extends Card {

    public MinionMissile() {
        addCastingCost(AnyOfCost.of(new DiscardCardTypeCost(null, null),
                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "a creature", false)));
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                        .addSpellEffect(DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 2));
    }
}
