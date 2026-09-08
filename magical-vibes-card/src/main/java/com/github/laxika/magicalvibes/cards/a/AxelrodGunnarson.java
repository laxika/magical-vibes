package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "CHR", collectorNumber = "72")
public class AxelrodGunnarson extends Card {

    public AxelrodGunnarson() {
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, SequenceEffect.of(
                new GainLifeEffect(1),
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)));
    }
}
