package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "231")
public class DiamondMare extends Card {

    public DiamondMare() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenColorPredicate()),
                new GainLifeEffect(1));
    }
}
