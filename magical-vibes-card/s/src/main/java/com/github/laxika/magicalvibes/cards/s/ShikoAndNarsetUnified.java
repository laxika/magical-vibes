package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlurryCopyOrDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "7")
public class ShikoAndNarsetUnified extends Card {

    public ShikoAndNarsetUnified() {
        addTrigger(Triggers.youCastSpell(), new FlurryCopyOrDrawTriggerEffect());
    }
}
