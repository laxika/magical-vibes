package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ISD", collectorNumber = "97")
public class DiregrafGhoul extends Card {

    public DiregrafGhoul() {
        // Diregraf Ghoul enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
