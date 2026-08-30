package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FirestormPhoenixReplacementEffect;

@CardRegistration(set = "LEG", collectorNumber = "147")
public class FirestormPhoenix extends Card {

    public FirestormPhoenix() {
        addEffect(EffectSlot.STATIC, new FirestormPhoenixReplacementEffect());
    }
}
