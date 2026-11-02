package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesGainLifePerCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "189")
@CardRegistration(set = "DDR", collectorNumber = "12")
public class JaddiLifestrider extends Card {

    public JaddiLifestrider() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TapCreaturesGainLifePerCreatureEffect(2));
    }
}
