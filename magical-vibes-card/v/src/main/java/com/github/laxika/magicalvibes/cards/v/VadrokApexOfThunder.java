package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CastTargetNoncreatureCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "214")
public class VadrokApexOfThunder extends Card {

    public VadrokApexOfThunder() {
        addTrigger(Triggers.mutates(), new CastTargetNoncreatureCardFromGraveyardEffect(3));
    }
}
