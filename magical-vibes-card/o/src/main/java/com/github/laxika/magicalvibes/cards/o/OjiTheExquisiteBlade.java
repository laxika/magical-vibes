package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "246")
public class OjiTheExquisiteBlade extends Card {

    public OjiTheExquisiteBlade() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2), new ScryEffect(2));

        var creatureYouControl = TargetFilters.creatureYouControl();
        target(creatureYouControl, 0, 1)
                .addTrigger(Triggers.youCastSpell().nthThisTurn(2), FlickerEffect.flickerTarget());
    }
}
