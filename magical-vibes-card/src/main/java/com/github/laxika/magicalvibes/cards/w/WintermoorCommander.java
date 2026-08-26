package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "205")
public class WintermoorCommander extends Card {

    public WintermoorCommander() {
        PermanentCount knightsYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.KNIGHT), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(new Fixed(2), knightsYouControl));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.KNIGHT),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another Knight you control"
        )).addEffect(EffectSlot.ON_ATTACK,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));
    }
}
