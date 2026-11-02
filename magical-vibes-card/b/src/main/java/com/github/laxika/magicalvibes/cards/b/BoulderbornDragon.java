package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "239")
public class BoulderbornDragon extends Card {

    public BoulderbornDragon() {
        addTrigger(Triggers.attacks(), new SurveilEffect(1));
    }
}
