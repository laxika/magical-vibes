package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "MH1", collectorNumber = "42")
public class BlizzardStrix extends Card {

    public BlizzardStrix() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                "Target must be another permanent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsAnotherPermanent(new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                FlickerEffect.exileTargetReturnAtEndStep()
        ));
    }
}
