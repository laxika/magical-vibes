package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "39")
public class WerefoxBodyguard extends Card {

    public WerefoxBodyguard() {
        target(new PermanentPredicateTargetFilter(nonFoxCreature(), "Target must be a non-Fox creature"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentUntilSourceLeavesEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(2)),
                "{1}{W}, Sacrifice Werefox Bodyguard: You gain 2 life."
        ));
    }

    private static PermanentPredicate nonFoxCreature() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.FOX))
        ));
    }
}
