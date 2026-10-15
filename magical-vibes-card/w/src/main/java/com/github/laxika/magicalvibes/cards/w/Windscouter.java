package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAtEndOfCombatEffect;

@CardRegistration(set = "PCY", collectorNumber = "53")
public class Windscouter extends Card {

    public Windscouter() {
        addEffect(EffectSlot.ON_ATTACK, new ReturnSelfToHandAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new ReturnSelfToHandAtEndOfCombatEffect());
    }
}
