package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryNinjutsuEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "298")
@CardRegistration(set = "MB2", collectorNumber = "534")
public class PanglacialShinobi extends Card {

    public PanglacialShinobi() {
        addStaticEffect(new LibraryNinjutsuEffect("{1}{U}"));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1));
    }
}
