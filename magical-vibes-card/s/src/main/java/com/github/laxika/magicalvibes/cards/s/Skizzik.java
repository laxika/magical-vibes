package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "145")
@CardRegistration(set = "INV", collectorNumber = "169")
public class Skizzik extends Card {

    public Skizzik() {
        addStaticEffect(new KickerEffect("{R}"));
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                new ConditionalEffect(new NotKicked(), new SacrificeSelfEffect()));
    }
}
