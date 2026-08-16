package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentsThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "222")
public class NiambiEsteemedSpeaker extends Card {

    public NiambiEsteemedSpeaker() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another creature you control"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ReturnTargetPermanentsThenEffect(EventStat.MANA_VALUE,
                        new GainLifeEffect(new EventValue())),
                "Return target creature you control to its owner's hand?"
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}{U}",
                List.of(
                        new DiscardCardTypeCost(new CardSupertypePredicate(CardSupertype.LEGENDARY), "legendary card"),
                        new DrawCardEffect(2)
                ),
                "{1}{W}{U}, {T}, Discard a legendary card: Draw two cards."
        ));
    }
}
