package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "794")
public class YoshimaruEverFaithful extends Card {

    public YoshimaruEverFaithful() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
