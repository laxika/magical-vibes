package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowExtraLoyaltyActivationEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "200")
public class OathOfTeferi extends Card {

    public OathOfTeferi() {
        // When Oath of Teferi enters, exile another target permanent you control.
        // Return it to the battlefield under its owner's control at the beginning of the next end step.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another permanent you control"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentAndReturnAtEndStepEffect());

        // You may activate the loyalty abilities of planeswalkers you control twice each turn
        // rather than only once.
        addEffect(EffectSlot.STATIC, new AllowExtraLoyaltyActivationEffect());
    }
}
