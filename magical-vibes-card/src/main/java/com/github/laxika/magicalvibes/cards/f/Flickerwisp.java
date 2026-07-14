package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "EVE", collectorNumber = "6")
public class Flickerwisp extends Card {

    public Flickerwisp() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                "Target must be another permanent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, FlickerEffect.exileTargetReturnAtEndStep());
    }
}
