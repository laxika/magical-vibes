package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "VOW", collectorNumber = "158")
@CardRegistration(set = "DBL", collectorNumber = "425")
public class FlameBlessedBolt extends Card {

    public FlameBlessedBolt() {
        addSpellEffect(DealDamageEffect.toTargetCreatureOrPlaneswalker(2).exilingIfItWouldDie());
    }
}
