package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "249")
@CardRegistration(set = "ECC", collectorNumber = "137")
@CardRegistration(set = "LCC", collectorNumber = "106")
public class ChimilTheInnerSun extends Card {

    public ChimilTheInnerSun() {
        addStaticEffect(new ControllerSpellsCantBeCounteredEffect());
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new DiscoverEffect(5));
    }
}
