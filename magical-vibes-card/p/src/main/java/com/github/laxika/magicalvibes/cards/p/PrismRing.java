package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "235")
public class PrismRing extends Card {

    public PrismRing() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenColorPredicate()),
                new GainLifeEffect(1));
    }
}
