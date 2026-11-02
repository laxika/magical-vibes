package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "37")
@CardRegistration(set = "C18", collectorNumber = "86")
public class DictateOfKruphix extends Card {

    public DictateOfKruphix() {
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), new DrawCardForTargetPlayerEffect(1));
    }
}
