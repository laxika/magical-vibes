package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardAdvantageSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "318")
@CardRegistration(set = "MB2", collectorNumber = "554")
public class TooferKeeperOfTheFullGrip extends Card {

    public TooferKeeperOfTheFullGrip() {
        addTrigger(Triggers.youCastSpell(), new CardAdvantageSpellCastTriggerEffect());
    }
}
