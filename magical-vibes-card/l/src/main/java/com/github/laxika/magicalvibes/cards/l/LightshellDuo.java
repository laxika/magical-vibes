package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "157")
@CardRegistration(set = "BLB", collectorNumber = "56")
public class LightshellDuo extends Card {

    public LightshellDuo() {
        // Prowess is a Scryfall-loaded keyword, so its boost is handled by the engine.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(2));
    }
}
