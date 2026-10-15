package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneOfEachCardTypeFromGraveyardEffect;

@CardRegistration(set = "NCC", collectorNumber = "72")
@CardRegistration(set = "NCC", collectorNumber = "172")
public class GrimeGorger extends Card {

    public GrimeGorger() {
        addEffect(EffectSlot.ON_ATTACK, new ExileUpToOneOfEachCardTypeFromGraveyardEffect());
    }
}
