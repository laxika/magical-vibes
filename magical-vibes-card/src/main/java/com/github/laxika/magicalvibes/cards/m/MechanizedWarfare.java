package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageToOpponentsFromRedOrArtifactSourcesEffect;

@CardRegistration(set = "BRO", collectorNumber = "139")
public class MechanizedWarfare extends Card {

    public MechanizedWarfare() {
        addEffect(EffectSlot.STATIC, new AdditionalDamageToOpponentsFromRedOrArtifactSourcesEffect(1));
    }
}
