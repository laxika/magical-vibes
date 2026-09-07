package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "M21", collectorNumber = "158")
@CardRegistration(set = "ELD", collectorNumber = "139")
public class ScorchingDragonfire extends Card {

    public ScorchingDragonfire() {
        addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(3, null, true));
    }
}
