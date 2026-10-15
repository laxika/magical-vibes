package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastTargetNoncreatureCardFromGraveyardEffect;

@CardRegistration(set = "IKO", collectorNumber = "214")
public class VadrokApexOfThunder extends Card {

    public VadrokApexOfThunder() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new CastTargetNoncreatureCardFromGraveyardEffect(3));
    }
}
