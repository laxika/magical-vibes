package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAtEndOfCombatEffect;

@CardRegistration(set = "ODY", collectorNumber = "93")
public class PhantomWhelp extends Card {

    public PhantomWhelp() {
        addEffect(EffectSlot.ON_ATTACK, new ReturnSelfToHandAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new ReturnSelfToHandAtEndOfCombatEffect());
    }
}
