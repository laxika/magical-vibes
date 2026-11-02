package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "57")
public class IlMhegPixie extends Card {

    public IlMhegPixie() {
        addTrigger(Triggers.attacks(), new SurveilEffect(1));
    }
}
