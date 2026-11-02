package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "220")
@CardRegistration(set = "RVR", collectorNumber = "238")
public class WhisperAgent extends Card {

    public WhisperAgent() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(1));
    }
}
