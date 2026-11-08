package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "224")
public class DiamondKnight extends Card {

    public DiamondKnight() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenColorPredicate()),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
