package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "LRW", collectorNumber = "92")
public class Surgespanner extends Card {

    public Surgespanner() {
        // Whenever this creature becomes tapped, you may pay {1}{U}. If you do,
        // return target permanent to its owner's hand.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                new MayPayManaEffect("{1}{U}",
                        ReturnToHandEffect.target(),
                        "Pay {1}{U} to return target permanent to its owner's hand?")));

        // Target filter for the may-pay bounce trigger: any permanent.
        setCastTimeTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent"));
    }
}
