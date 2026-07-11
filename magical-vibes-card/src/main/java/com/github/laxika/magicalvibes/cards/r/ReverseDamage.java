package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenSourceAndGainLifeEffect;

@CardRegistration(set = "9ED", collectorNumber = "35")
public class ReverseDamage extends Card {

    public ReverseDamage() {
        addEffect(EffectSlot.SPELL, new PreventNextDamageFromChosenSourceAndGainLifeEffect());
    }
}
