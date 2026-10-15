package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceDownEffect;

@CardRegistration(set = "WHO", collectorNumber = "38")
@CardRegistration(set = "WHO", collectorNumber = "355")
@CardRegistration(set = "WHO", collectorNumber = "643")
@CardRegistration(set = "WHO", collectorNumber = "946")
public class CyberConversion extends Card {

    public CyberConversion() {
        addEffect(EffectSlot.SPELL, TurnTargetCreatureFaceDownEffect.asCyberman());
    }
}
