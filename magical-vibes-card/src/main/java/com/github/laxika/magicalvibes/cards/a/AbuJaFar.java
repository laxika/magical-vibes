package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOnDeathEffect;

@CardRegistration(set = "CHR", collectorNumber = "1")
public class AbuJaFar extends Card {

    public AbuJaFar() {
        addEffect(EffectSlot.ON_DEATH, new DestroyCombatOpponentsOnDeathEffect());
    }
}
