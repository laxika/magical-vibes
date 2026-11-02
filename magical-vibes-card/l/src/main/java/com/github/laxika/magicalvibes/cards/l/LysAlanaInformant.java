package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "181")
public class LysAlanaInformant extends Card {

    public LysAlanaInformant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(1));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new SurveilEffect(1));
    }
}
