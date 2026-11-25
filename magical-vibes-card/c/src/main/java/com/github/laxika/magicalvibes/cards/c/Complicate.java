package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "76")
public class Complicate extends Card {

    public Complicate() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(3));

        addCycling("{2}{U}");
        addTrigger(Triggers.youCycleThisCard(), new MayEffect(
                UnlessPaysEffect.counterUnlessPays(1), "Counter target spell unless its controller pays {1}?"));
    }
}
