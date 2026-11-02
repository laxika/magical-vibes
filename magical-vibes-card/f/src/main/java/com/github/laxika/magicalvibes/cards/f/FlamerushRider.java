package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "99")
@CardRegistration(set = "MOC", collectorNumber = "279")
@CardRegistration(set = "LTC", collectorNumber = "216")
@CardRegistration(set = "C19", collectorNumber = "143")
public class FlamerushRider extends Card {

    public FlamerushRider() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}{R}"))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReturnSelfToHandIfDashCostPaidEffect());

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Must target another attacking creature."
        )).addTrigger(Triggers.attacks(),
                CreateTokenCopyOfTargetPermanentEffect.tappedAndAttackingWithTargetChoiceExiledAtEndOfCombat());
    }
}
