package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesNameWithAnotherPermanentPredicate;

@CardRegistration(set = "INV", collectorNumber = "45")
public class Winnow extends Card {

    public Winnow() {
        PermanentNotPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        target(new PermanentPredicateTargetFilter(
                nonland,
                "Target must be a nonland permanent"
        )).addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetPermanentMatches(new PermanentSharesNameWithAnotherPermanentPredicate()),
                new DestroyTargetPermanentAndAllWithSameNameEffect(nonland)
        )).addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
