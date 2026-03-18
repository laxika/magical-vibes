package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOM", collectorNumber = "9")
public class GlimmerpointStag extends Card {

    public GlimmerpointStag() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                "Target must be another permanent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentAndReturnAtEndStepEffect());
    }
}
