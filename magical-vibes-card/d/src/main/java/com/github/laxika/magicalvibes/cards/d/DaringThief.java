package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "JOU", collectorNumber = "36")
public class DaringThief extends Card {

    public DaringThief() {
        setMultiTargetConstraint(MultiTargetConstraint.SHARE_CARD_TYPE);

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "First target must be a nonland permanent you control"));

        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                "Second target must be a permanent an opponent controls"))
                .addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new MayEffect(
                        ExchangeControlOfTargetPermanentsEffect.forControlledTargetsSharingCardType(
                                new PermanentNotPredicate(new PermanentIsLandPredicate())),
                        "Exchange control of the two target permanents?"));
    }
}
